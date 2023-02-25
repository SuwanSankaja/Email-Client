// 200580E
//======================= Special Instruction Before running this program=========================================
// you should add both javax.mail.jar and activation-1.1.1.jar libraries to the classpath to run this code properly
//import libraries
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
public class Email_Client {
    public static void main(String[] args) {
        // email use to test this code
        String email ="emailclienttestdemo@gmail.com" ;
        String password ="amqysfpwrljxahpg";
        String Recipient_List = "clientList.txt";
        String Sent_List = "Sent_Emails.txt";
        Email_Manager My_Email_Manager = new Email_Manager(email,password);
        My_Email_Manager.Run(Recipient_List,Sent_List);
        Scanner input = new Scanner(System.in);
        loop: while (true){
            System.out.println("Enter option type: \n"
                            + "1 - Adding a new recipient\n"
                            + "2 - Sending an email\n"
                            + "3 - Printing out all the recipients who have birthdays\n"
                            + "4 - Printing out details of all the emails sent\n"
                            + "5 - Printing out the number of recipient objects in the application\n"
                            + "===================Press any other button to Exit============================");
            String option = input.nextLine();
            switch(option){
                case "1":
                    System.out.println("Input Format :\n\t" +
                            "official:<name>,<email>,<designation>\n\t"+
                            "office_friend:<name>,<email>,<designation>,<birthday>\n\t" +
                            "personal:<name>,<nickname>,<email>,<birthday>\n"+ "Enter Recipient details:- ");
                    String New_Recip_Data = input.nextLine();
                    My_Email_Manager.Add_Recipients(New_Recip_Data,Recipient_List);
                    break;
                case "2":
                    System.out.println("input format - email,subject,content");
                    String Line = input.nextLine();
                    String[] mail_details=Line.split(",");
                    try{
                        My_Email_Manager.sendEmail(mail_details[0],
                                mail_details[1],mail_details[2]);}
                    catch(Exception exception){
                        System.out.println("Your input type is Invalid, Please check...");
                    }
                    finally{break;}

                case "3":
                    System.out.println("input format - yyyy/mm/dd (ex: 2022/08/07)");
                    String birthdate = input.nextLine();
                    while(!birthdate.matches("\\d\\d\\d\\d\\/\\d\\d\\/\\d\\d")) {
                        System.out.println("Wrong Format, Enter Again..");
                        birthdate = input.nextLine();}
                    My_Email_Manager.Print_Wishables(birthdate);
                    break;

                case "4":
                    System.out.println("input format - yyyy/mm/dd (ex: 2022/08/07)");
                    String Date = input.nextLine();
                    while(!Date.matches("\\d\\d\\d\\d\\/\\d\\d\\/\\d\\d")) {
                        System.out.println("Wrong Format, Enter Again..");
                        Date = input.nextLine();}
                    My_Email_Manager.Print_Sent_Mails(Date);
                    break;

                case "5":
                    System.out.println("Number of Recipients : "+My_Email_Manager.get_Recip_Count());
                    break;

                default:
                    input.close();
                    My_Email_Manager.Stop(Sent_List);
                    System.out.println("Email Client is Turning off");
                    break loop;
            }

        }
    }

}
//============================================================================================================================
//============================================================================================================================
//defining all class blueprints
//defining main recipient class
abstract class Recipient{
    private String name;
    private String email;
    private static int Recip_Count;
    public Recipient(String name,String email){
        this.name=name;
        this.email=email;
        Recip_Count++;}

    public String getName(){
        return name;}

    public String getEmail(){
        return email;}

    public static int get_Recip_Count() {
        return Recip_Count;}
}
//defining Official class
class Official extends Recipient{
    private String designation;
    public Official(String name, String email, String designation){
        super(name,email);
        this.designation=designation;}

    public String getDesignation(){
        return designation;}
}
//defining OfficialFriend class
class OfficeFriend extends Official implements Wishable {
    private String Bday;

    public OfficeFriend(String name,String email,String desingnation,String bday){
        super(name,email,desingnation);
        this.Bday=bday;}

    @Override
    public String get_Bday(){
        return Bday;}
    @Override
    public String get_wished() {
        return "Wish you a Happy Birthday. Suwan";}
}
//defining Personal class
class Personal extends Recipient implements Wishable{
    private String nick_name;
    private String Bday;
    public Personal(String name,String email,String nick_name,String bday){
        super(name,email);
        this.Bday=bday;
        this.nick_name=nick_name;
    }
    public String getNick_name(){
        return nick_name;
    }
    @Override
    public String get_Bday(){
        return Bday;}

    @Override
    public String get_wished() {
        return "Hugs and Love on your Birthday. Su1";}
}
//interface for birthday wishes
interface Wishable{

    public String get_wished();
    public String get_Bday();
}
//===========================================================================================================================
//===========================================================================================================================
//main class of the Email_Client for manage all recipients and its functions
class Email_Manager {
    final String username;
    final String password;
    private String Current_Date;
    private ArrayList<Wishable> Wishables;
    private ArrayList<Recipient> Recipients;
    private HashMap<String,ArrayList<String>> Sent_Mails;
    public Email_Manager(String username,String password) {
        this.username = username;
        this.password = password;
        this.Wishables = new ArrayList<>();
        this.Recipients = new ArrayList<>();
        this.Sent_Mails = new HashMap<String, ArrayList<String>>();
    }
    //method to start the Email_Client running.
//this includes
// getting current date
// deserialize Sent_List
// send wishes to wishables
// read recipient details
    public void Run(String Recipient_List,String Sent_List) {
        System.out.println("Email_Client starts Running...");
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/mm/dd");
        Current_Date = formatDate.format(new Date());
        Read_Recipients(Recipient_List);
        Deserialize_Mails(Sent_List);
        try {
            if(!(Sent_Mails.containsKey(Current_Date))){
                Wishing();} }
        catch(NullPointerException exception) {
            System.out.println("Error occur while running the program");}
    }
    //Add Recipients to List
    public void Add_Recipients(String details,String list) {
        Recipient recip = Match_Recip_Type(details);
        if (recip!=null)
        {
            Update_Recipients(details,list);
            Recipients.add(recip);}
    }
    //sends Email with given subject and body to given email address using javax.mail
    public void sendEmail(String To, String subject, String body) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);}
                });
        try {
            System.out.println("Wait while email is Sending.....");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("emailclienttestdemo@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(To));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            Update_Sent_Mails(subject, To);
            System.out.println("Email has send successfully!");}
        catch (MessagingException e) {
            System.out.println("Failed to sent the Email.!!");}
    }
    //print recipients who have birthday on the given date
    public void Print_Wishables(String date)
    {
        boolean No_Birthday = true;
        for(Wishable wish: Wishables )
        {
            if (wish.get_Bday().substring(4).equals(date.substring(4)))
            {
                No_Birthday = false;
                Recipient recip = (Recipient)wish;
                System.out.println(recip.getName()+wish.get_Bday()+"\n");
            }
        }
        if(No_Birthday) {
            System.out.println("No Birthday");}
    }
    //print the sent mails on a given date
    public void Print_Sent_Mails(String date) {
        try {
            for (String sent:Sent_Mails.get(date)) {
                System.out.println(sent);}}
        catch(NullPointerException e) {
            System.out.println("No Emails Found");}}
    //gives the total number of recipient objects in the application
    public int get_Recip_Count() {
        return Recipient.get_Recip_Count();}
    //stop the Email_Client
    public void Stop(String list) {
        Save_Sent_Mails(list);}
    // method to read recipients from "ClientList.txt"
    private void Read_Recipients(String list) {
        try {
            File Recip_file = new File(list);
            FileReader reader = new FileReader(Recip_file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.equals("")) {
                    Recipients.add(Match_Recip_Type(line)); }}
            bufferedReader.close();}
        catch (IOException exception) {
            System.out.println("Error Occured while reading from ClientList.txt.");}}
    // method to add Update new recipients to the file
    private void Update_Recipients(String recip_data, String list) {
        try {
            File Recip_file = new File(list);
            FileWriter writer = new FileWriter(Recip_file.getName(), true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("\n" + recip_data);
            bufferedWriter.close();}
        catch(IOException Exception) {
            System.out.println("Error Occured while writing to the file");}}
    // method to creating new recipient objects
    private Recipient Match_Recip_Type(String data){
        try{
            String [] line = data.split(":",2);
            String[] recipient_details = line[1].split(",");
            Recipient recipient;
            switch(line[0].toUpperCase()){
                case "OFFICIAL":
                    recipient = new
                            Official(recipient_details[0],recipient_details[1],recipient_details[2]);
                    return recipient;
                case "OFFICE_FRIEND":
                    recipient = new
                            OfficeFriend(recipient_details[0],recipient_details[1],recipient_details[2],recipient_details[3]);
                    Wishables.add((Wishable)recipient);
                    return recipient;
                case "PERSONAL":
                    recipient = new
                            Personal(recipient_details[0],recipient_details[2],recipient_details[1],recipient_details[3]);
                    Wishables.add((Wishable)recipient);
                    return recipient;
                default:
                    System.out.println("Invalid recipient");
                    return null;}}
        catch(Exception ss){
            System.out.println("Error with you Input");
            return null;
        }
    }
    //wish to recipient who have birthday on current date
    private void Wishing() {
        for(Wishable wish: Wishables ) {
            if (wish.get_Bday().substring(4).equals(Current_Date.substring(4))) {
                Recipient recip = (Recipient)wish;
                sendEmail(recip.getEmail(), "Happy Birthday", wish.get_wished());}}
        System.out.println("Wishes have sent");}
    //update sentMails
    private void Update_Sent_Mails(String subject, String email){
        try {
            if(Sent_Mails.containsKey(Current_Date)){
                Sent_Mails.get(Current_Date).add(subject+" , "+email);}
            else {
                ArrayList<String> mail_data = new ArrayList<String>();
                mail_data.add(subject+" , "+email);
                Sent_Mails.put(Current_Date, mail_data);}}
        catch(NullPointerException exception) {
            ArrayList<String> mail_data = new ArrayList<String>();
            mail_data.add(subject+" , "+email);
            Sent_Mails.put(Current_Date, mail_data);}
    }
    //Deserialize sent derail objects from the text file
    @SuppressWarnings("unchecked")
    private void Deserialize_Mails(String list) {
        try {
            FileInputStream file_input = new FileInputStream(list);
            ObjectInputStream object_output = new ObjectInputStream(file_input);
            Sent_Mails = (HashMap<String, ArrayList<String>>)object_output.readObject();
            object_output.close();}
        catch (IOException exception){
            System.out.println("Error occur when reading Deserializing the objects.");
            return;}
        catch(ClassNotFoundException exception){
            System.out.println("Class Error occur when Deserializing the objects.");
            return;}
    }
    //Serialize objects to the text file
    private void Save_Sent_Mails(String list) {
        try {
            FileOutputStream file_output = new FileOutputStream(list);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(file_output);
            objectOutputStream.writeObject(Sent_Mails);
            objectOutputStream.close();
        }catch (IOException exception){
            System.out.println("Error occur when Serializing the objects.");}
    }
}
 