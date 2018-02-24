function [exec_time, test_ran]= execution_time(inf_m,cov_m,time_m)
executed_inf_m = inf_m;
if(~isempty(inf_m))
    for i = 1:length(inf_m(:,1))
        % find the first test case that kills the mutant
        kill = find(cov_m(i,:));
        if(~isempty(kill))
            executed_inf_m(i,(kill(1)+1):end)=0;
        end
    end
    exec_time = sum(executed_inf_m,1)*time_m;
    test_ran = sum(sum(executed_inf_m,2));
else 
    exec_time=0;
    test_ran=0;
end
    
end