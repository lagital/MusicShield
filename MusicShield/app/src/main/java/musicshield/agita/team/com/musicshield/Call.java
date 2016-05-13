package musicshield.agita.team.com.musicshield;

/**
 * Created by pborisenko on 5/13/2016.
 */
public class Call {
    public String number;
    public String date_time;
    public DBHelper.CallType status;

    Call(String i_number, String i_date_time, DBHelper.CallType i_type){
        number = i_number;
        date_time = i_date_time;
        status = i_type;
    }
}
