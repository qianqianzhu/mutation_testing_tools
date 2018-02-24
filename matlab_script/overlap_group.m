function [group,overhead]=overlap_group(weak_m,touch_m,mutInfo)
tic;
% find distinct mutant groups
%fprintf('%d,%d\n',size(touch_m,1),length(mutInfo));
new_m = horzcat(touch_m,weak_m,mutInfo);
[~,ia,ic]=unique(new_m,'rows');
ng = length(ia);
group = cell(ng,2);
for i = 1:ng
    is_element = ismember(ic,i);
    % mutants
    group{i,1} = find(is_element);
    row_group = new_m(is_element,:);
    % tests
    group{i,2} = find(row_group(1,:));
end

group(cellfun(@isempty,group(:,2)),:)=[];

overhead=toc;
end