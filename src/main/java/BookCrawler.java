import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BookCrawler {
    protected List<AudioBook> audioBooks = new ArrayList<AudioBook>();
    protected WebDriver driver;
    WebDriverWait wait;
    private static final Logger logger = LogManager.getLogger(BookCrawler.class);
    public int HEIGHT_OF_ONE_BOOK_ROW=250;
    List<WebElement> webElementList;
    Actions actions;

    @BeforeClass
    public static void setUp(){
        WebDriverManager.chromedriver().setup();
    }

    @Before
    public void setUpDriver(){
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("user-data-dir=src/main/Test Data/ChromeProfile");
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver,60);
        actions = new Actions(driver);
    }
    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void Test() throws Exception{
        try {
            loadAllBooksOnThePage();
        } catch (Exception e) {
            logger.warn("Not all books loaded. Execution will be continued. Error info {}",e);
        }

        try {
            addAudioBookToBookList(audioBooks);
        } catch (Exception e) {
            logger.warn("Not all books has been added to the list.Execution will be continued. Error info {}", e);
        }

        for(AudioBook audioBook : audioBooks)
        {
            try {
                getBookInfo(audioBook);
            } catch (Exception e) {
                logger.warn("Could not get the info for book {} due to error {}", audioBook.getUrl(), e);
            }
            logger.info("Book information updated");
        }

        try {
            CsvFile.importDataToCsvFile(AudioBook.getCsvHeader(), getDataToCsvFile(audioBooks),"MIFBooks.csv");
        } catch (IOException e) {
            logger.warn("Not all book info has been imported to csv file due to error {}", e);
        }

    }


    public List<String[]> getDataToCsvFile(List<AudioBook> list){
        List<String[]> listForCss = new ArrayList<>();
        for(AudioBook item : list){
            listForCss.add(item.getCsvView());
        }
        return listForCss;
    }

    public void addAudioBookToBookList(List<AudioBook> listBook){
        webElementList = driver.findElements(By.cssSelector("div.c-continuous-list > div.lego-book"));
        logger.info(" Found {} books", webElementList.size());
        for (WebElement element: webElementList) {
            listBook.add(new AudioBook(element.findElement(By.tagName("a")).getAttribute("href")));
            logger.info("Book added, total book {}",listBook.size());
        }
    }


    public void getBookInfo(AudioBook audioBook){
        driver.get(audioBook.getUrl());

        try {
            audioBook.setName(driver.findElement(By.cssSelector("h1.header.active.p-sky-title >span")).getText());
        } catch (NoSuchElementException e) {
            audioBook.setName("not defined");
        }

          try {
            audioBook.setPrice(driver.findElement(By.cssSelector("div[ng-if=\"bookData.types.audiobook.sale\"] nobr")).getText());
        } catch (NoSuchElementException e) {
           audioBook.setPrice("not defined");
        }

        try {
            audioBook.setAuthor(driver.findElement(By.cssSelector("div.l-book-description > div.b-description > div.authors span.author.active")).getText());
            if(audioBook.getAuthor().length()==0){audioBook.setAuthor("not defined");}
        } catch (NoSuchElementException e) {
            audioBook.setAuthor("not defined");
        }
        try {
            audioBook.setRefToFragment(driver.findElement(By.cssSelector("div[ng-if=\"bookData.types.audiobook.examples.length\"] a")).getAttribute("href"));
        } catch (NoSuchElementException e) {
            audioBook.setRefToFragment("not defined");
        }

        logger.info(audioBook);
    }



    public void loadAllBooksOnThePageSlowVersion(){
        driver.get("https://www.mann-ivanov-ferber.ru/books/allbooks/?booktype=audiobook");
        int i=0;
        int startScrollPoint = 0;
        while (true){
            WebElement loader = driver.findElement(By.cssSelector("div.js-page-loader.page-loader"));
            int loaderPointY = loader.getLocation().getY();

            if(loaderPointY == startScrollPoint){
               logger.info("Books not loaded, waiting for loading completed ....");
               try {
                    wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.cssSelector("div.js-page-loader.page-loader"),loader.getText())));
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 10)");
                    logger.info("Additional scroll done");
                }
            }

            logger.info("----------------Iteration #{} : waiting text {};  Loader Point Y {} ----------------",i,loader.getText(),loaderPointY);
            if(loader.getAttribute("style").equals("display: none;")){
                logger.info("All books on the page loaded!");
                break;
            }
            else {
                logger.info("Starting scrolling until loader will be reached");
                for (int j = startScrollPoint; j < loaderPointY; j+=HEIGHT_OF_ONE_BOOK_ROW) {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 270)");
                    logger.info("Start scroll to point Y {} ; Loader Point Y = {};", j, loaderPointY);

                }
                startScrollPoint = loaderPointY;
                logger.info("----------------Iteration done----------------");
            }
            i++;
        }
    }


    public void loadAllBooksOnThePage(){
        driver.get("https://www.mann-ivanov-ferber.ru/books/allbooks/?booktype=audiobook");
        int i=0;
        logger.info("Starting book loading.It can takes up to 15 mins...");
        while (true){
            WebElement loader = driver.findElement(By.cssSelector("div.js-page-loader.page-loader"));
            logger.info("----------------Iteration #{} ----------------",i);
            logger.info("Start loading books");
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 250)");
            List<WebElement> notLoadedBooksOnThePage = driver.findElements(By.cssSelector("div.c-continuous-list > div.lego-book img[class=\"lego-book__cover-image js-cover-image lazyload-initialized\"]"));

            if(notLoadedBooksOnThePage.size()!=0) {
                logger.info("Amount of not loaded books {}",notLoadedBooksOnThePage.size());
                logger.info("Waiting for full loading");
                for (WebElement book : notLoadedBooksOnThePage) {
                    actions.moveToElement(book).perform();
                    try {
                        wait.until(ExpectedConditions.attributeContains(book, "class", "image-loaded"));
                    } catch (TimeoutException e) {
                        logger.info("Timeout exceed. Let's try one more time");
                    }
                }
                logger.info("Image loaded");
            }
            else{
                if(loader.getAttribute("style").equals("display: none;")){
                    logger.info("All books on the page loaded!");
                    break;
                }
                logger.info("Books still not loaded, waiting for loading completed ....");
                WebElement newLoader = driver.findElement(By.cssSelector("div.js-page-loader.page-loader"));
                try {
                    wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(By.cssSelector("div.js-page-loader.page-loader"),newLoader.getText())));
                } catch (Exception e) {
                    ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 10)");
                    logger.info("Timeout exceed. Let's try one more time");
                }
            }
            logger.info("----------------Iteration done----------------");
            i++;
        }
    }

}

