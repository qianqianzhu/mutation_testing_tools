function [total_res,total_goal,exec_time,overhead,test_ran,total_mut,total_tst,test_exec_time]=run_com_mt(workpath,isManual)
fid = fopen(strcat(workpath,'/targetClasses.txt'));  % targeClasses file
fid2 = fopen('./metadata/task_data/mutationOperator.txt');
all_classes = textscan(fid, '%s');
mutOp2id = textscan(fid2,'%s');
mutOp2id = mutOp2id{1,1};
fclose('all');

classNo=length(all_classes{1,1});
total_res = cell(classNo,7);

group = cell(classNo,6);
total_goal = zeros(classNo,3); % col1-strong, col2-weak, col3-touch
overhead = zeros(classNo,6);
exec_time = zeros(classNo,9);
accuracy_weak = 1;
test_ran = zeros(classNo,5);
test_exec_time = 0; % normal testing

total_mut = zeros(classNo,1);
total_tst = zeros(classNo,1);
for cid = 1:classNo
    oneClass = all_classes{1,1}(cid);
    %oneClass = 'nl.tudelft.jpacman.board.Board';
    % manual tests
    %workpath = '/Users/zhuqianqian/workspace/mutation/report/jpacman(class_level)';
    %target='jfreechart-1.0.19';
    if(isManual)
        dir=strcat(workpath,'/report/manual/',oneClass,'/');
        [exit_code,weak_m,strong_m,touch_m,time_m,mutant_info]=prepare_matrix(cell2mat(dir),1);
        if(exit_code==0)
            total_mut(cid) = size(weak_m,1);
            if(total_tst==0)
                total_tst(cid) = size(weak_m,2);
                test_exec_time = sum(time_m);
            end
        end

    if(exit_code==0)
        % cov-based execution time
        [exec_time(cid,1),test_ran(cid,1)]= execution_time(touch_m,strong_m,time_m);
        % inf-based execution time
        [exec_time(cid,2),test_ran(cid,2)]= execution_time(weak_m,strong_m,time_m);
        % mutOp info
        [~, mutOpInfo] = ismember(mutant_info{1,2},mutOp2id);
        % mutLoc info
        if(isManual)
            %fprintf('%d\n',length(mutant_info{1,2}));
            newstring = cellfun(@(c,idx)c(idx+1:end),mutant_info{1,3},strfind(mutant_info{1,3},':'),'UniformOutput',false);
            newstring2 = cellfun(@(c,idx)c(1:idx-1),newstring,strfind(newstring,' '),'UniformOutput',false);
            mutLocInfo = cellfun(@str2num,newstring2);
        else
            mutLocInfo=mutant_info{1,3};
        end

        % overlap grouping
        % pure random
        [group{cid,1},overhead(cid,1)]=overlap_group(weak_m,touch_m,zeros(size(weak_m,1),1));
        [total_res{cid,1},total_goal(cid,1),exec_time(cid,3),test_ran(cid,3)]= mutant_selection(weak_m,strong_m,time_m,group{cid,1},-1);
        % with mutation operators
        [group{cid,2},overhead(cid,2)]=overlap_group(weak_m,touch_m,mutOpInfo);
        [total_res{cid,2},~,exec_time(cid,4),test_ran(cid,4)]= mutant_selection(weak_m,strong_m,time_m,group{cid,2},-1);
        % with mutation location
        [group{cid,3},overhead(cid,3)]=overlap_group(weak_m,touch_m,mutLocInfo);
        [total_res{cid,3},~,exec_time(cid,5),test_ran(cid,5)]= mutant_selection(weak_m,strong_m,time_m,group{cid,3},-1);

        % fca grouping
        % pure random
        [group{cid,4},overhead(cid,4),comp]=fca_group(weak_m,touch_m,zeros(size(weak_m,1),1));
        [total_res{cid,4},~,exec_time(cid,6),test_ran(cid,6)]= mutant_selection(weak_m,strong_m,time_m,group{cid,4},comp);
        %disp(comp);
        % with mutation operators
        [group{cid,5},overhead(cid,5),comp]=fca_group(weak_m,touch_m,mutOpInfo);
        [total_res{cid,5},~,exec_time(cid,7),test_ran(cid,7)]= mutant_selection(weak_m,strong_m,time_m,group{cid,5},comp);
        % with mutation location
        [group{cid,6},overhead(cid,6),comp]=fca_group(weak_m,touch_m,mutLocInfo);
        [total_res{cid,6},~,exec_time(cid,8),test_ran(cid,8)]= mutant_selection(weak_m,strong_m,time_m,group{cid,6},comp);

        % random
        total_res{cid,7} = random(touch_m,strong_m,time_m);
        exec_time(cid,9) = mean(cell2mat(total_res{cid,3}(:,3)));
        test_ran(cid,9) = mean(cell2mat(total_res{cid,3}(:,4)));

        % weak mutation
        total_goal(cid,2) = sum(sum(weak_m,2)>0);
        weak_result = sum(weak_m,2)>0;
        strong_result = sum(strong_m,2)>0;
        conf_matrix = confusionmat(strong_result,weak_result);
        if(size(conf_matrix,1)==2)
            accuracy_weak = (conf_matrix(1,1)+conf_matrix(2,2))/sum(sum(conf_matrix));
        end
        total_res{cid,8} = accuracy_weak;

        % touched mutation
        total_goal(cid,3) = sum(sum(touch_m,2)>0);

    else
        continue
    end
end

end
