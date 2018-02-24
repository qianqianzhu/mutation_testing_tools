function [total_group,overhead,total_mul]=fca_group(weak_m,touch_m,mutInfo)
tic;

% group by mutInfo
[~,ia,ic]=unique(mutInfo);
mutInfo_ng = length(ia);
mutInfo_group = cell(mutInfo_ng,1);
for i = 1:mutInfo_ng
    is_element = ismember(ic,i);
    mutInfo_group{i} = find(is_element);
end

total_group = cell(mutInfo_ng,1);
total_mul = cell(mutInfo_ng,1);
% fca grouping in each mutInfo group
for i = 1:mutInfo_ng
    included_mid = mutInfo_group{i};
    new_touch_m = touch_m(included_mid,:);
    new_weak_m = weak_m(included_mid,:);
    %new_m = horzcat(new_touch_m,new_weak_m);
    %new_m = new_weak_m;
    [group,~,mul_new]=sub_fca_group(new_weak_m,new_touch_m);
    % map mid as the mid is different from the origin weak_m
    group(:,1) =cellfun(@(x)included_mid(x),group(:,1),'UniformOutput',false);
    total_group{i,1} = group;
    %     total_group{i,2} = group(:,2);
    total_mul{i} = mul_new;
    %disp(mul_new);
end
total_group(cellfun(@isempty,total_group),:)=[];
total_mul(cellfun(@isempty,total_mul),:)=[];

total_group=vertcat(total_group{:});
total_mul=vertcat(total_mul{:});

overhead=toc;
end