package com.example.project4task2;

/**
 * @author Wenting Yu
 * Andrew ID: wy2
 * Email: wy2@andrew.cmu.edu
 * Last Modified: 3/24/23
 * This program is a class made to store json information about a show
 */
public class Title {
    String id;
    String title;
    String year;
    String imdb_id;
    String tmdb_id;
    String tmdb_type;
    String type;

    @Override
    public String toString() {
        return title + " " + year;
    }
}
