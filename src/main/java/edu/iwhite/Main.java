package edu.iwhite;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper; //class ObjectMapper, @JsonIgnore
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        // generate a HashTable of Strings (to be businessIDs) mapped to FrequencyTables
        // (which will be used for establishing similarity based on lemmas in review texts)
        HashTable<String, FrequencyTable> reviews = new HashTable<>();
        HashTable<String, Business> businesses = new HashTable<>();

        // create iterator for Reviews
        final ObjectReader reviewMapper = new ObjectMapper().readerFor(Review.class);
        MappingIterator<Object> reviewIterator = reviewMapper.readValues(new File("C:\\Users\\ianw1\\Desktop\\Yelp Reviews\\review.json"));

        // create iterator for Businesses
        final ObjectReader businessMapper = new ObjectMapper().readerFor(Business.class);
        MappingIterator<Object> businessIterator = businessMapper.readValues(new File("C:\\Users\\ianw1\\Desktop\\Yelp Reviews\\business.json"));

        final StanfordLemmatizer lemmatizer = new StanfordLemmatizer();

        int count = 0;

        // parse reviews and add FrequencyTable if one does not exist for a businessID, create a FrequencyTable of
        // lemmas mapped to frequencies for each Review
        while (reviewIterator.hasNext() && count++ < 10000) {
            Review review = ((Review) reviewIterator.next());
            if (!reviews.contains(review.getBusinessID()))
                reviews.add(review.getBusinessID(), new FrequencyTable());

            FrequencyTable ft = reviews.get(review.getBusinessID());

            List<String> lemmas = lemmatizer.lemmatize(review.getText());

            for (String lemma : lemmas) {
                ft.add(lemma);
            }
        }

        // parse businesses and exclude businesses without at least 1 review
        while (businessIterator.hasNext()) {
            Business business = ((Business) businessIterator.next());
            if (reviews.contains(business.getBusinessID()))
                businesses.add(business.getBusinessID(), business);
        }

        new GUI(businesses, reviews);
    }
}