package Library.libDB;

public class Reader{
    private int ReaderID;
    private String FirstName;
    private String LastName;
    private String Address;
    private String PhoneNumber;
    private int Limits;

    public Reader(int ReaderID,String FirstName,String LastName,String Address,String PhoneNumber,int Limits){
        this.ReaderID=ReaderID;
        this.FirstName=FirstName;
        this.LastName=LastName;
        this.Address=Address;
        this.PhoneNumber=PhoneNumber;
        this.Limits=Limits;
    }

    public Reader(){}

    public int getReaderID(){return ReaderID;}

    public String getFirstName(){return FirstName;}

    public String getLastName(){return LastName;}

    public String getAddress(){return Address;}

    public String getPhoneNumber(){return PhoneNumber;}

    public int getLimits(){return Limits;}

    public void setReaderID(int ReaderID){this.ReaderID=ReaderID;}

    public void setFirstName(String FirstName){this.FirstName=FirstName;}

    public void setLastName(String LastName){this.LastName=LastName;}

    public void setAddress(String Address){this.Address=Address;}

    public void setPhoneNumber(String PhoneNumber){this.PhoneNumber=PhoneNumber;}

    public void setLimits(int Limits){this.Limits=Limits;}

    @Override
    public String toString(){
        return "Reader[ReaderID="+ReaderID+"Name="+FirstName+LastName+"]";
    }
}
