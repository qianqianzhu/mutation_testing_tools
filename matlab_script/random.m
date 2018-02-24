function random_res=random(weak_m,strong_m,time_m)
random_res = cell(100,5);
nmut = size(weak_m,1);
total_goal = sum(sum(strong_m,2)>0);
fprintf('%d\n',nmut);
ng = ceil(nmut/10);
% random picking mutants
for run = 1:100
    fprintf('running random %d\n',run);
    
    picked_m = randperm(nmut,ng);
    if(ng~=0)
        score = sum(sum(strong_m(picked_m,:),2)>0)*(nmut/ng);
    else
        score = 0;
    end
    random_res{run,1}=score;
    random_res{run,2}=picked_m;
    picked_weak_m = weak_m(picked_m,:);
    picked_strong_m = strong_m(picked_m,:);
    [random_res{run,3},random_res{run,4}]= execution_time(picked_weak_m,picked_strong_m,time_m);
    
   if(nmut~=0)
        error_rate = (random_res{run,1} - total_goal)/nmut;
    elseif((random_res{run,1} - total_goal)==0)
        error_rate=0;
    else
        error_rate = 1;
    end
    random_res{run,5} = error_rate;
    
    
end
end