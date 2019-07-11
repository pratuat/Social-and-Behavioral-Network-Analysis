package utils;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;


/**
 * This builder create a new TweetTerm computing also its SAX
 * @author Gabriele Avellino
 * @author Giovanni Trappolini
 */
public class sax {
    // Size of SAX representation Alphabet
    private int alphabetSize;
    // Sax threshold
    private double nThreshold;
    // alphabet component for SAX

    private final Alphabet na;
    private SAXProcessor sp;

    /**
     * Initialize Builder params
     * @param alphabetSize
     * @param nThreshold
     */
    public sax(int alphabetSize, double nThreshold) {
        this.alphabetSize = alphabetSize;
        this.nThreshold = nThreshold;
        this.na = new NormalAlphabet();
        this.sp = new SAXProcessor();
    }


    public String createSAX( double[] timeSeries) throws SAXException {
        // Compute SAX
        SAXRecords res = sp.ts2saxByChunking(timeSeries, timeSeries.length, na.getCuts(alphabetSize), nThreshold);
        // return SAX representation
        return res.getSAXString("");

    }
}
