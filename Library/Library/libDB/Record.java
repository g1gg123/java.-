package Library.libDB;

public class Record {
    private int RecordID;
    private String ISBN;
    private int ReaderID;
    private String BorrowingDate;
    private String ReturnDate;

    public Record() {}
    public Record(int RecordID,String ISBN,int readerID,String borrowingDate,String returnDate) {
        this.RecordID = RecordID;
        this.ISBN = ISBN;
        this.ReaderID = readerID;
        this.BorrowingDate = borrowingDate;
        this.ReturnDate = returnDate;
    }
    public int getRecordID() {return RecordID;}
    public String getISBN() {return ISBN;}
    public int getReaderID() {return ReaderID;}
    public String getBorrowingDate() {return BorrowingDate;}
    public String getReturnDate() {return ReturnDate;}
    public void setRecordID(int RecordID) {this.RecordID = RecordID;}
    public void setISBN(String ISBN) {this.ISBN = ISBN;}
    public void setReaderID(int ReaderID) {this.ReaderID = ReaderID;}
    public void setBorrowingDate(String BorrowingDate) {this.BorrowingDate = BorrowingDate;}
    public void setReturnDate(String ReturnDate) {this.ReturnDate = ReturnDate;}

    @Override
    public String toString() {
        return "Record[RecordID="+RecordID+"ISBN="+ISBN+"ReaderId="+ReaderID+"]";
    }
}
