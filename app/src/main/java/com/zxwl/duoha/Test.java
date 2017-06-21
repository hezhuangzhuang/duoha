package com.zxwl.duoha;

/**
 * author：hw
 * data:2017/6/21 15:05
 */

public class Test {


    private void printGuessStatistics(char candidate, int count) {
        String number;
        String verb;
        String pluralModifier;

        if (0 == count) {
            number = "no";
            verb = "are";
            pluralModifier = "s";
        } else if (1 == count) {
            number = "1";
            verb = "is";
            pluralModifier = "";
        } else {
            number = Integer.toString(count);
            verb = "are";
            pluralModifier = "s";
        }
        String guessMessage = String.format("Threr %s %s %s %s", verb, number, candidate, pluralModifier);
    }


    private String number;
    private String verb;
    private String pluralModifier;

    public String make(char candidate, int count) {
        createPluralDependent(count);
        return String.format("Threr %s %s %s %s", verb, number, candidate, pluralModifier);
    }

    private void createPluralDependent(int count) {
        switch (count){
            case 0:
                thereAreNoletters();
                break;

            case 1:
                thereIsOneLetter();
                break;

            default:
                thereAreManyLetters(count);
                break;
        }
    }

    private void thereAreManyLetters(int count) {
        number = Integer.toString(count);
        verb = "are";
        pluralModifier = "s";
    }

    private void thereIsOneLetter() {
        number = "1";
        verb = "is";
        pluralModifier = "";
    }

    private void thereAreNoletters() {
        number = "no";
        verb = "are";
        pluralModifier = "s";
    }

}
