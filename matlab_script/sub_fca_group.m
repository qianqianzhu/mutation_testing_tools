function [group,overhead,mul_new]=sub_fca_group(weak_m,touch_m)
tic;

new_m = horzcat(touch_m,weak_m);
%new_m = weak_m;
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

% find maximal groupings of fca: remove unclosed concepts
test_len = cellfun(@length,group(:,1));
[~,sort_idx]=sort(test_len);
group =group(sort_idx,:);
mul_new = cellfun(@length,group(:,1));
for current = 1: size(group,1)-1
    for next = current+1 : size(group,1)-1
        if(~any(ismember(group{current,2},group{next,2})==0,2))
            %group{next,2}=[group{next,2},group{current,2}];
            group{current,2}=[];
            mul_new(next)=mul_new(next)+mul_new(current);
            break;
        end
    end
end

% remove unclosed concepts
mul_new(cellfun(@isempty,group(:,2)))=[];
%group(cellfun(@isempty,group(:,2)),:)=[];

group(cellfun(@isempty,group(:,2)),:)=[];

overhead=toc;
end