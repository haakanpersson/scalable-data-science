// Databricks notebook source exported at Wed, 2 Mar 2016 03:09:01 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC # Word Count on US State of the Union (SoU) Addresses
// MAGIC 
// MAGIC * Word Count in big data is the equivalent of 'Hello World' in programming
// MAGIC * We count the number of occurences of each word in the first and last (2016) SoU addresses.

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC An interesting analysis of the textual content of the *State of the Union (SoU)* addresses by all US presidents was done in:
// MAGIC * [Alix Rule, Jean-Philippe Cointet, and Peter S. Bearman, Lexical shifts, substantive changes, and continuity in State of the Union discourse, 1790–2014, PNAS 2015 112 (35) 10837-10844; doi:10.1073/pnas.1512221112](http://www.pnas.org/content/112/35/10837.full).
// MAGIC 
// MAGIC 
// MAGIC ![](http://www.pnas.org/content/112/35/10837/F5.large.jpg)
// MAGIC 
// MAGIC [Fig. 5](http://www.pnas.org/content/112/35/10837.full). A river network captures the flow across history of US political discourse, as perceived by contemporaries. Time moves along the x axis. Clusters on semantic networks of 300 most frequent terms for each of 10 historical periods are displayed as vertical bars. Relations between clusters of adjacent periods are indexed by gray flows, whose density reflects their degree of connection. Streams that connect at any point in history may be considered to be part of the same system, indicated with a single color. 
// MAGIC 
// MAGIC ## Let us investigate this dataset ourselves!
// MAGIC 1. We first get the source text data by scraping and parsig from [http://stateoftheunion.onetwothree.net/texts/index.html](http://stateoftheunion.onetwothree.net/texts/index.html) as explained in 
// MAGIC [scraping and parsing SoU addresses](/#workspace/scalable-data-science/xtraResources/sdsDatasets/scraperUSStateofUnionAddresses).
// MAGIC * This data is already made available in DBFS, our distributed file system.
// MAGIC * We only do the simplest word count with this data in this notebook and will do more sophisticated analyses in the sequel (including topic modeling, etc).

// COMMAND ----------

// MAGIC %md
// MAGIC ###DBFS and dbutils - where is this dataset in our distributed file system?
// MAGIC * Since we are on the databricks cloud, it has a file system called DBFS
// MAGIC * DBFS is similar to HDFS, the Hadoop distributed file system
// MAGIC * dbutils allows us to interact with dbfs.
// MAGIC * The 'display' command displays the list of files in a given directory in the file system.

// COMMAND ----------

display(dbutils.fs.ls("dbfs:/datasets/sou")) // Cntrl+Enter to display the files in dbfs:/datasets/sou

// COMMAND ----------

// MAGIC %md
// MAGIC Let us display the *head* or the first few lines of the file `dbfs:/datasets/sou/17900108.txt` to see what it contains using `dbutils.fs.head` method.  
// MAGIC `head(file: String, maxBytes: int = 65536): String` -> Returns up to the first 'maxBytes' bytes of the given file as a String encoded in UTF-8
// MAGIC as follows:

// COMMAND ----------

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt",673) // Cntrl+Enter to get the first 673 bytes of the file (which corresponds to the first five lines)

// COMMAND ----------

// MAGIC %md
// MAGIC ##### You Try!
// MAGIC Modify ``xxxx` in the cell below to read the first 1000 bytes from the file.

// COMMAND ----------

dbutils.fs.head("dbfs:/datasets/sou/17900108.txt", xxxx) // Cntrl+Enter to get the first 1000 bytes of the file

// COMMAND ----------

// MAGIC %md
// MAGIC ### Read the file into Spark Context as an RDD of Strings
// MAGIC * The `textFile` method on the available `SparkContext` `sc` can read the text file `dbfs:/datasets/sou/17900108.txt` into Spark and create an RDD of Strings
// MAGIC   * but this is done lazily until an action is taken on the RDD `sou17900108`!

// COMMAND ----------

val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt") // Shift+Enter to read in the textfile as RDD[String]

// COMMAND ----------

// MAGIC %md
// MAGIC ### Perform some actions on the RDD
// MAGIC * Each String in the RDD `sou17900108` represents one line of data from the file and can be made to perform one of the following actions:
// MAGIC   * count the number of elements in the RDD `sou17900108` (i.e., the number of lines in the text file `dbfs:/datasets/sou/17900108.txt`) using `sou17900108.count()`
// MAGIC   * display the contents of the RDD using `take` or `collect`.

// COMMAND ----------

sou17900108.count() // <Shift+Enter> to count the number of elements in the RDD

// COMMAND ----------

sou17900108.take(5) // <Shift+Enter> to display the first 5 elements of RDD

// COMMAND ----------

sou17900108.take(5).foreach(println) // <Shift+Enter> to display the first 5 elements of RDD line by line

// COMMAND ----------

sou17900108.collect // <Cntrl+Enter> to display all the elements of RDD

// COMMAND ----------

### Cache the RDD in (distributed) memory to avoid recreating it for each action
* Let's use `.cache()` after creating an RDD so that it is in memory after the first action (and thus avoid reconstruction for subsequent actions).
  * count the number of elements in the RDD `sou17900108` (i.e., the number of lines in the text file `dbfs:/datasets/sou/17900108.txt`) using `sou17900108.count()`
  * display the contents of the RDD using `take` or `collect`.

// COMMAND ----------

// Shift+Enter to read in the textfile as RDD[String] and cache it in distributed memory
val sou17900108 = sc.textFile("dbfs:/datasets/sou/17900108.txt")
sou17900108.cache() // cache the RDD in memory

// COMMAND ----------

sou17900108.count() // Shift+Enter during this count action the RDD is constructed from texfile and cached

// COMMAND ----------

sou17900108.count() // Shift+Enter during this count action the cached RDD is used (notice less time taken by the same command)

// COMMAND ----------

sou17900108.take(5) // <Cntrl+Enter> to display the first 5 elements of the cached RDD

// COMMAND ----------

// MAGIC %md
// MAGIC ### Transform lines to words
// MAGIC * We need to loop through each line and split the line into words
// MAGIC * For now, let us split using whitespace
// MAGIC * More sophisticated regular expressions can be used to split the line (as we will see soon)

// COMMAND ----------

sou17900108
.flatMap(line => line.split(" "))
.take(100)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Naive word count
// MAGIC At a first glace, to do a word count of George Washingtons SoU address, we are templed to do the following:
// MAGIC  * just break each line by the whitespace character " " and find the words using a `flatMap`
// MAGIC  * then do the `map` with the closure `word => (word, 1)` to initialize each `word` with a integer count of `1` 
// MAGIC     * ie., transform each word to a *(key, value)* pair or `Tuple` such as `(word, 1)`
// MAGIC  * then count all *value*s with the same *key* (`word` is the Key in our case) by doing a
// MAGIC    * `reduceByKey(_+_)` 
// MAGIC  * and finally `collect()` to display the results.

// COMMAND ----------

sou17900108
.flatMap( line => line.split(" ") )
.map( word => (word, 1) )
.reduceByKey(_+_)
.collect()

// COMMAND ----------

// MAGIC %md
// MAGIC Unfortunately, as you can see from the `collect` above:
// MAGIC * the words have punctuations at the end which means that the same words are being counted as different words. Eg: importance
// MAGIC * empty words are being counted
// MAGIC 
// MAGIC So we need a bit of `regex`'ing or regular-expression matching (all readily available from Scala via Java String types).
// MAGIC 
// MAGIC We will cover the three things we want to do with a simple example from Middle Earth!
// MAGIC * replace all multiple whitespace characters with one white space character " "
// MAGIC * replace all punction characters we specify within `[` and `]` such as `[,?.!:;]` by the empty string "" (i.e., remove these punctuation characters)
// MAGIC * convert everything to lower-case.

// COMMAND ----------

val example = "Master, Master!   It's me, Sméagol... mhrhm*%* But they took away our precious, they wronged us. Gollum will protect us..., Master, it's me Sméagol."

// COMMAND ----------

example.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
       .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
       .toLowerCase() // converting to lower-case

// COMMAND ----------

// MAGIC %md
// MAGIC ### More sophisticated word count
// MAGIC We are now ready to do a word count of George Washington's SoU on January 8th 1790 as follows:

// COMMAND ----------

val wordCount_sou17900108 = 
sou17900108
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x,1))
    .reduceByKey(_+_)
    
wordCount_sou17900108.collect()

// COMMAND ----------

val top10 = wordCount_sou17900108.sortBy(_._2, false).collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ### Doing it all together for George Washington and Barrack Obama

// COMMAND ----------

//sc.textFile("dbfs:/datasets/sou/17900108.txt") // George Washington's first SoU
sc.textFile("dbfs:/datasets/sou/20160112.txt")   // Barrack Obama's second SoU
    .flatMap(line => 
         line.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
             .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
             .toLowerCase() // converting to lower-case
             .split(" "))
    .map(x => (x,1))
    .reduceByKey(_+_)
    .sortBy(_._2, false)
    .collect()

// COMMAND ----------

// MAGIC %md
// MAGIC ## HOMEWORK 
// MAGIC * HOWEWORK WordCount 1: `sortBy`
// MAGIC * HOMEWROK WordCount 2: `dbutils.fs`

// COMMAND ----------

// MAGIC %md
// MAGIC ##### HOMEWORK WordCount 1. `sortBy`
// MAGIC 
// MAGIC Let's understand `sortBy` a bit more carefully.

// COMMAND ----------

val example = "Master, Master!   It's me, Sméagol... mhrhm*%* But they took away our precious, they wronged us. Gollum will protect us..., Master, it's me Sméagol."

// COMMAND ----------

val words = example.replaceAll("\\s+", " ") //replace multiple whitespace characters (including space, tab, new line, etc.) with one whitespace " "
       .replaceAll("""([,?.!:;])""", "") // replace the following punctions characters: , ? . ! : ; . with the empty string ""
       .toLowerCase() // converting to lower-case
       .split(" ")

// COMMAND ----------

val rddWords = sc.parallelize(words)

// COMMAND ----------

rddWords.take(10)

// COMMAND ----------

val wordCounts = rddWords
                  .map(x => (x,1))
                  .reduceByKey(_+_)

// COMMAND ----------

val top10 = wordCounts.sortBy(_._2, false).take(10)

// COMMAND ----------

// MAGIC %md
// MAGIC Make your code easy to read for other developers ;)  
// MAGIC Use 'case classes' with well defined variable names that everyone can understand

// COMMAND ----------

val top10 = wordCounts.sortBy({
  case (word, count) => count
}, false).take(10)

// COMMAND ----------

// MAGIC %md
// MAGIC If you just want a total count of all words in the file

// COMMAND ----------

rddWords.count

// COMMAND ----------

// MAGIC %md
// MAGIC ##### HOMEWORK WordCount 2: `dbutils.fs`
// MAGIC 
// MAGIC Have a brief look at what other commands dbutils.fs supports.  We will introduce them as needed.

// COMMAND ----------

dbutils.fs.help // some of these were used to ETL this data into dbfs:/datasets/sou 

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)