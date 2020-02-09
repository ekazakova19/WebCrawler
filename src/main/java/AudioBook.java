

public class AudioBook {
    private String url;
    private String name;
    private String author;
    private String price;
    private String refToFragment;

    public AudioBook(String url) {
        this.url = url;
    }
    @Override
    public String toString() {
        return "AudioBook{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", price='" + price + '\'' +
                ", refToFragment='" + refToFragment + '\'' +
                '}';
    }

    public String[] getCsvView(){
        return new String[] {this.name, this.author, this.price, this.refToFragment, this.url};
    }

    public static String[] getCsvHeader(){
        return new String[] {"name","author","price","referenceToFragment", "link"};
    }


    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAuthor() { return author; }

    public void setAuthor(String author) { this.author = author; }

    public String getPrice() { return price; }

    public void setPrice(String price) { this.price = price; }

    public String getRefToFragment() { return refToFragment; }

    public void setRefToFragment(String refToFragment) { this.refToFragment = refToFragment; }

}
