package vanderbilt.cs279.org.dispatchmobile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sam on 1/31/2017.
 */

public class UserInformation {
    String firstName;
    String lastName;
    String sessionId;
    String email;
    String id;
    String profession;
    int numActive;
    int numDone;

    List<Skill> skills;
    List<Skill> possibleSkills;

    String emailHash;

    @Override
    public String toString() {
        return(firstName+" "+lastName+" "+sessionId);
    }

    public String[] possibleSkillsArray(){
        String[] tmp = new String[possibleSkills.size()];
        for(int i=0; i<tmp.length;i++){
            tmp[i] = possibleSkills.get(i).name;
        }
        return tmp;
    }

    public Set<String> initializeSkillSet(){
        Set<String> tmp = new HashSet<>();
        for(int i=0; i<skills.size();i++){
            tmp.add(skills.get(i).name);
        }
        return tmp;
    }

    public boolean[] createAlreadyChecked(Set<String> mySkills){
        boolean[] tmp = new boolean[possibleSkills.size()];
        for(int i=0; i<tmp.length; i++){
            if(mySkills.contains(possibleSkills.get(i).name))
                tmp[i] = true;
            else
                tmp[i] = false;
        }
        return tmp;
    }

    public String getSkills(Set<String> mySkills){
        String tmp = "";
        for (String s : mySkills) {
            tmp+=s;
            if(tmp.length() != 0)
                tmp+=", ";
        }
        return tmp;
    }

    public long[] getSkillsIds(Set<String> mySkills){
        long[] tmp = new long[mySkills.size()];
        int counter = 0;
        for(int i=0; i<possibleSkills.size(); i++){
            if(mySkills.contains(possibleSkills.get(i).name)){
                tmp[counter] = possibleSkills.get(i).id;
                counter++;
            }
        }
        return tmp;
    }
}
