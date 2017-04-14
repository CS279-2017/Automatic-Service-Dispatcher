package vanderbilt.cs279.org.dispatchmobile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Sam on 1/31/2017.
 *
 * This is a POJO that contains all the user information, including the direct fields
 * (name, profession, etc.) and lists of nested POJOs (skills and possibleSkills)
 *
 * This is built to be used with the Retrofit library, which will automatically create
 * this POJO using the UserInformation JSON response from the server. Reference the
 * GlowAPI class for the Retrofit API
 */

public class UserInformation {

    /*
        User Information
     */
    String firstName;
    String lastName;
    String email;
    String id;
    String profession;
    int numActive;
    int numDone;

    // Session ID used by the server to identify this user's current session.
    String sessionId;

    List<Skill> skills;
    List<Skill> possibleSkills;

    String emailHash;

    /**
     * Returns a String that contains this user's name and sessionId
     * @return
     */
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

    /**
     * Returns a set of this user's skills
     * @return
     */
    public Set<String> initializeSkillSet(){
        Set<String> tmp = new HashSet<>();
        for(int i=0; i<skills.size();i++){
            tmp.add(skills.get(i).name);
        }
        return tmp;
    }

    /**
     * Returns an array of booleans, each entry representing if this user has each respective skill
     * in the input skill set
     * @param mySkills
     * @return
     */
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

    /**
     * Convert the skill set input into a String
     * @param mySkills
     * @return
     */
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
