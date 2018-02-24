function [exit_code,branch_m,inf_m,cov_m,time_m,mutant_info_gen]=generate_matrix(dir)
exit_code = -1;
fprintf('processing dir: %s\n',dir);
if (exist(strcat(dir,'mutants_info.csv'),'file')~=2) % necessary files not exists
    branch_m=0;
    inf_m=0;
    cov_m=0;
    time_m=0;
    mutant_info_gen=0;
    return
else
    exit_code=0;
end

% prepare matrix
fid = fopen(strcat(dir,'mutation_sum.csv'));
fid2 = fopen(strcat(dir,'test_sum.csv'));
fid3 = fopen(strcat(dir,'mutants_info.csv'));
m2t0 = textscan(fid, '%s %n %n %n %n %n %n %s','Delimiter',',');
sum_info = textscan(fid2,'%s %d %d','Delimiter',',');
mutant_info_gen = textscan(fid3, '%n %s %n %s','Delimiter','\t');

% calculate the size of the mutant2test matrix
row = zeros(length(sum_info{1,1}),1);
col = zeros(length(sum_info{1,1}),1);
for i=1:length(sum_info{1,1})
    % row: sum of the mutant number
    row(i) = sum_info{1,3}(i);
    % col: sum of the test case number
    col(i) = sum_info{1,2}(i);
end

% for appending to a big matrix
is_new = zeros(length(sum_info{1,1}),1);
for i = 1:length(sum_info{1,1})
    if(i>1)
        is_new(i) = is_new(i-1)+row(i)*col(i);
    else
        is_new(i) = row(i)*col(i)+1;
    end
end

% id matching: old id -> new id (as Evosuite didn't generate sequatial id sometimes)
oid = mutant_info_gen{1,1}+1;
[~,oid2nid]=ismember(1:max(oid),oid);
%disp(oid2nid);

% prepare mutant2test matrix for all elements
inf_m = zeros(sum(row),sum(col));
imp_m = zeros(sum(row),sum(col));
cov_m = zeros(sum(row),sum(col));
branch_m = zeros(sum(row),sum(col));
time_m = zeros(sum(col),1);

row_shift = 0;
col_shift = 0;

% process the original matrix m2t0 to generate one big matrix
for i=1:length(m2t0{1,1})
    % if it is a new class, append to diagnose, add row shift and column shift
    [is_mem, loc] = ismember(i,is_new);
    if(is_mem)
        row_shift = sum(row(1:loc));
        col_shift = sum(col(1:loc));
    end
    %fprintf('oid2nid:%d,%d\n',m2t0{1,3}(i)+1,oid2nid(m2t0{1,3}(i)+1));
    mutantId = oid2nid(m2t0{1,3}(i)+1)+row_shift;
    testId = m2t0{1,2}(i)+1+col_shift;
    % transfer to 0-1 matrix
    branch_dis = (m2t0{1,5}(i)==0);
    inf_dis = 1-m2t0{1,6}(i);
    imp_dis = (m2t0{1,7}(i)==0);
    is_covered = strcmp(m2t0{1,8}(i),'true');
    
    branch_m(mutantId,testId)=branch_dis;
    inf_m(mutantId,testId)=inf_dis;
    imp_m(mutantId,testId)=imp_dis;
    cov_m(mutantId,testId)=is_covered;
    
    time_m(testId)=m2t0{1,4}(i);
    
    %fprintf('processing testId:%d mutantId:%d branch_dis:%d inf_dis:%d imp_dis:%d covered:%d\n',...
    %    testId,mutantId,m2t0{1,5}(i),inf_dis,imp_dis,is_covered);
    %testId = testId+1;
end

fclose('all');
% for checking the results
%covered_goal=sum(sum(cov_m,2)>0);
%impacted_goal=sum(sum(imp_m,2)>0);
%fprintf('covered goals:%d impacted goals:%d\n',covered_goal,impacted_goal);
% save data
%save('joda1.mat','inf_m','imp_m','cov_m','time_m','branch_m');
end
