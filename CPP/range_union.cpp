#include <iostream>
#include <vector>
#include <map>

using namespace std;

/**
 * Definition for an interval.
 * 
 **/
struct Interval {
	int start;
	int end;
	Interval() : start(0), end(0) {}
	Interval(int s, int e) : start(s), end(e) {}
};

bool Comp(const Interval &a, const Interval &b)
{
    return a.start<b.start;
};

class Solution {
public:
    vector<Interval> merge(vector<Interval> &intervals) {
        // IMPORTANT: Please reset any member data you declared, as
        // the same Solution instance will be reused for each test case.
        vector<Interval> re;
        if(intervals.size()==0) return re;
        sort(intervals.begin(),intervals.end(),Comp);
        Interval tmp=*(intervals.begin());
        vector<Interval>::iterator i;
        for(i=intervals.begin(),i++;i!=intervals.end();i++){
            if(tmp.end>=i->start){
                tmp.end=max(tmp.end,i->end);
            }
            else{
                re.push_back(tmp);
                tmp=*i;
            }
        }
        re.push_back(tmp);
        return re;
    }
};

int main(int argc, char** argv)
{
	Solution slu;
	vector<Interval> ranges = { {1, 4}, {3, 10}, {9, 100}, {2, 4}, {8, 200}};
	vector<Interval> ranges_result = slu.merge(ranges);

	for (const auto& it : ranges_result)
	{
		cout << "[" << it.start << ", " << it.end << "]" << endl;
	}

	return 0;
}
