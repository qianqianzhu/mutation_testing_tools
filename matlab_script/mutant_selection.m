function [total_res,total_goal,exec_time,test_ran]= mutant_selection(weak_m,strong_m,time_m,group,comp)

ng = size(group,1);
total_goal = sum(sum(strong_m,2)>0);
total_run = 100;
total_mut = size(weak_m,1);
total_res = cell(total_run,7);
 
% mutant selection
for run = 1:total_run
    fprintf('selecting mutants %d time\n',run);
    picked_mutant = zeros(ng,1);
    
    mul = zeros(ng,1);
    %fprintf('picked mutants:');
    for i = 1:ng
        % picking mutants
        picked_m = randi([1,length(group{i,1})],1);
        mul(i) = length(group{i,1});
        picked_mutant(i) = group{i,1}(picked_m);
    end
    mul = mul(mul~=0);
    
    picked_weak_m = weak_m(picked_mutant,:);
    picked_strong_m = strong_m(picked_mutant,:);
    
    tem = sum(picked_strong_m,2)>0;
    
    % prediction
    if(comp>0)
        total_res{run,1} = comp'*tem;
    elseif(comp==-1)
        total_res{run,1} = mul'*tem;
    else
        total_res{run,1}=0;
    end
    
    % selected_mutant
    total_res{run,2} = picked_mutant;
    % execution time
    [total_res{run,3},total_res{run,4}]= execution_time(picked_weak_m,picked_strong_m,time_m);
    
    % confusion matrix
    actual_result = double(sum(strong_m,2)>0);
    predict_result = zeros(length(actual_result),1);
   
    for groupid = 1:size(group,1)
        %disp(groupid);
        %disp(size(picked_mutant));
        predict_result(group{groupid}) = actual_result(picked_mutant(groupid));
    end
    %display([actual_result,predict_result]);
    conf_matrix = confusionmat(actual_result, predict_result);
    total_res{run,6} = conf_matrix;
    
    % accuarcy
    if(size(conf_matrix,1)==2)
        accuracy = (conf_matrix(1,1)+conf_matrix(2,2))/sum(sum(conf_matrix));
    else
        accuracy = 1;
    end
    if(total_mut~=0)
        error_rate = (total_res{run,1} - total_goal)/total_mut;
    elseif((total_res{run,1} - total_goal)==0)
        error_rate=0;
    else
        error_rate = 1;
    end
    
    total_res{run,5} = error_rate;
    total_res{run,7} = accuracy;
    
end
exec_time = mean(cell2mat(total_res(:,3)));
test_ran = mean(cell2mat(total_res(:,4)));

end