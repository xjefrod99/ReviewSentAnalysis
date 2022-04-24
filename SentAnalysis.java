/*
 * Please see submission instructions for what to write here. 
 */

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import java.util.*;

import java.nio.charset.Charset;


import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SentAnalysis {

	final static File TRAINFOLDER = new File("train");
	
	public static HashMap<String, Integer> posdict = new HashMap<>();
	public static HashMap<String, Integer> negdict = new HashMap<>();

	public static double numPos_label;

	public static double numNeg_label;
	
	public static double numPos_words;

	public static double numNeg_words;
		
	public static void main(String[] args) throws IOException
	{	
		ArrayList<String> files = readFiles(TRAINFOLDER);		
		
		train(files);
		//if command line argument is "evaluate", runs evaluation mode
		if (args.length==1 && args[0].equals("evaluate")){
			evaluate();
		}
		else{//otherwise, runs interactive mode
			@SuppressWarnings("resource")
			Scanner scan = new Scanner(System.in);
			System.out.print("Text to classify>> ");

			String textToClassify = scan.nextLine();
			System.out.println("Result: "+classify(textToClassify));
		}
		
	}
	

	
	/*
	 * Takes as parameter the name of a folder and returns a list of filenames (Strings) 
	 * in the folder.
	 */
	public static ArrayList<String> readFiles(File folder){
		
		System.out.println("Populating list of files");
		
		//List to store filenames in folder
		ArrayList<String> filelist = new ArrayList<String>();
		
	
		for (File fileEntry : folder.listFiles()) {
	        String filename = fileEntry.getName();
	        filelist.add(filename);
		}
	    
		/*
		for (String fileEntry : filelist) {
	        System.out.println(fileEntry);
		}
		
		System.out.println(filelist.size());
		*/
		
		
		return filelist;
	}
	
	

	
	/*
	 * TO DO
	 * Trainer: Reads text from data files in folder datafolder and stores counts 
	 * to be used to compute probabilities for the Bayesian formula.
	 * You may modify the method header (return type, parameters) as you see fit.
	 */
	public static void train(ArrayList<String> files) throws FileNotFoundException
	{
		
		/**
		 * 1. read one file at a time
		 * extract label from name
		 *  MOVIES/ACTOR-1-4321
		 * if pos: add to positive dict
		 *  
		 * 
		 * else: agg to neg 
		 *
		 * 
		 * helper(list of words in review, label)
		 * it'll edit the corresponding dict
		 */
		
		for (int i = 0; i < files.size(); i++) { //files.size()
			String filename = files.get(i);
			int index = filename.indexOf('-');

			if (filename.charAt(index + 1) == '1'){
				//System.out.println(readReview(filename));
				dictionarize(readReview(filename), false);
				
			}
			
			else if(filename.charAt(index + 1) == '5'){
				
				dictionarize(readReview(filename), true);
				
			}

		}
		//System.out.println(" pos dict ");
		//System.out.println(posdict.toString());
		//System.out.println(" neg dict ");

		//System.out.println(negdict.toString());

		
		 
	}
	

	///takes in a filename and returns list of all words
	public static List<String> readReview(String filename){

		try{
			Path path = Paths.get("/Users/owner/Desktop/AI/Hw4/train/" + filename);
			Charset charset = StandardCharsets.UTF_8;
			List<String> lines = Files.readAllLines(path, charset);
			String temp = lines.get(0);
			List<String> items = Arrays.asList(temp.split(" "));
			return items;
		}
		catch(Exception e){
			//System.out.println("ERROR READING : " + filename);
			return null;
		}
		
	}


	//Takes in the list of words in a review and adds it to the appropiate dictionary
	public static void dictionarize(List<String> words, boolean label){
		//System.out.println(words);
		if (label){
			for (String s: words){
				s = s.toLowerCase().replaceAll("[^a-zA-Z0-9]+","");
				if (s.length() > 1){
					if (posdict.containsKey(s))
						posdict.put(s, posdict.get(s)+1);
					else
						posdict.put(s,1);
				}
				numPos_words++;
			}
			numPos_label++;
			}
		else{
			for (String s: words){
				s = s.toLowerCase().replaceAll("[^a-zA-Z0-9]+","");
				if (s.length() > 1){
					if (negdict.containsKey(s))
						negdict.put(s, negdict.get(s)+1);
					else
						negdict.put(s,1);
				}
				numNeg_words++;
			}
			numNeg_label++;
			}
	
	}
	
	

	/*
	 * Classifier: Classifies the input text (type: String) as positive or negative
	 */
	public static String classify(String text)
	{
		List<String> words = Arrays.asList(text.split(" "));
		//calculate conditional probability, return 5 if text is likely to be positive or 1 if neg
		if (conditionalprobability(words) == 5) {
			return "Positive";
		}
		return "Negative";
		
	}

	public static double PR(boolean x){
		//takes a boolean, if we want positive label input true, else input false
		double prx = 1.0 / (numPos_label+numNeg_label);

		if(x){
			return numPos_label * prx;
		}
		return numNeg_label*prx;
	}

	public static int conditionalprobability(List<String> words){

		double posOcurrence = 0.0, negOcurrence =0.0, neg_prob =0.0, pos_prob = 0.0;

		for( String word : words){
			// calculate conditional prob of each word here

			if ( !negdict.containsKey(word)){
				neg_prob += ( (1/numNeg_words ) * PR(false));
				//neg_prob += ( (negOcurrence/numNeg_words ) * PR(false));

			}
			else if ( negdict.containsKey(word)){
				negOcurrence = negdict.get(word);
				neg_prob += ( (negOcurrence/numNeg_words ) * PR(false));
			}

			if(posdict.containsKey(word)){
				posOcurrence = posdict.get(word);
				pos_prob += ( (posOcurrence/numPos_words ) * PR(true) );
			}
			else if( !posdict.containsKey(word)){
				//if word not in dict, then we put a pseudo count
				pos_prob += ( (1/numPos_words ) * PR(true) );
			}
		}

		if (pos_prob > neg_prob){
			return 5;
		}
		return 1;

	}


	
	
	
	

	/*
	 * TO DO
	 * Classifier: Classifies all of the files in the input folder (type: File) as positive or negative
	 * You may modify the method header (return type, parameters) as you like.
	 */
	public static void evaluate() throws FileNotFoundException {
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		
		System.out.print("Enter folder name of files to classify: ");
		String foldername = scan.nextLine();
		File folder = new File(foldername);
		
		ArrayList<String> filesToClassify = readFiles(folder);
		
	}
	
	
	
}
