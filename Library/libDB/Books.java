package Library.libDB;

public class Books{
    private String ISBN;
    private String Title;
    private String Authors;
    private String Publisher;
    private int EditionNumber;
    private String PublicationDate;
    private String Type;

    public Books(){}

    public Books(String ISBN,String Title,String Authors,String Publisher,int EditionNumber,String PublicationDate,String Type){
        this.ISBN=ISBN;
        this.Title=Title;
        this.Authors=Authors;
        this.Publisher=Publisher;
        this.EditionNumber=EditionNumber;
        this.PublicationDate=PublicationDate;
        this.Type=Type;
    }

    public String getISBN(){return ISBN;}

    public void setISBN(String ISBN){this.ISBN=ISBN;}

    public String getTitle(){return Title;}

    public void setTitle(String Title){this.Title=Title;}

    public String getAuthors(){return Authors;}

    public void setAuthors(String Author){this.Authors=Author;}

    public String getPublisher(){return Publisher;}

    public void setPublisher(String Publisher){this.Publisher=Publisher;}

    public int getEditionNumber(){return EditionNumber;}

    public void setEditionNumber(int EditionNumber){this.EditionNumber=EditionNumber;}

    public String getPublicationDate(){return PublicationDate;}

    public void setPublicationDate(String PublicationDate){this.PublicationDate=PublicationDate;}

    public String getType(){return Type;}

    public void setType(String Type){this.Type=Type;}


    //调试时用
    @Override
    public String toString(){
        return "Book[ISBN="+ISBN+" Title="+Title+"]";
    }
}
