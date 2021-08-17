/*
 * $ProjectName$
 * $ProjectRevision$
 * -----------------------------------------------------------
 * $Id: BasicStream.java,v 1.1 2003/08/08 19:48:22 jarnbjo Exp $
 * -----------------------------------------------------------
 *
 * $Author: jarnbjo $
 *
 * Description:
 *
 * Copyright 2002-2003 Tor-Einar Jarnbjo
 * -----------------------------------------------------------
 *
 * Change History
 * -----------------------------------------------------------
 * $Log: BasicStream.java,v $
 *
 */

package de.jarnbjo.ogg;

import java.io.*;
import java.util.*;

/**
 * Implementation of the <code>PhysicalOggStream</code> interface for reading
 * an Ogg stream from a URL. This class performs
 *  no internal caching, and will not read data from the network before
 *  requested to do so. It is intended to be used in non-realtime applications
 *  like file download managers or similar.
 */

public class BasicStream implements PhysicalOggStream {

   private boolean closed=false;
   private InputStream sourceStream;
   final private Object drainLock=new Object();
   final private LinkedList pageCache=new LinkedList();
   final private long numberOfSamples=-1;
   private int position=0;

   final private HashMap logicalStreams=new HashMap();
   private OggPage firstPage;

   public BasicStream(InputStream sourceStream) throws OggFormatException, IOException {
      firstPage=OggPage.create(sourceStream);
      position+=firstPage.getTotalLength();
      LogicalOggStreamImpl los=new LogicalOggStreamImpl(this, firstPage.getStreamSerialNumber());
      logicalStreams.put(firstPage.getStreamSerialNumber(), los);
      los.checkFormat(firstPage);
   }

   @Override
   public Collection getLogicalStreams() {
      return logicalStreams.values();
   }

   @Override
   public boolean isOpen() {
      return !closed;
   }

   @Override
   public void close() throws IOException {
      closed=true;
      sourceStream.close();
   }

   public int getContentLength() {
      return -1;
   }

   public int getPosition() {
      return position;
   }

   int pageNumber=2;

   @Override
   public OggPage getOggPage(int index) throws IOException {
      if(firstPage!=null) {
         OggPage tmp=firstPage;
         firstPage=null;
         return tmp;
      }
      else {
         OggPage page=OggPage.create(sourceStream);
         position+=page.getTotalLength();
         return page;
      }
   }

   private LogicalOggStream getLogicalStream(int serialNumber) {
      return (LogicalOggStream)logicalStreams.get(serialNumber);
   }

   @Override
   public void setTime(long granulePosition) throws IOException {
      throw new UnsupportedOperationException("Method not supported by this class");
   }

	/** 
	 *  @return always <code>false</code>
	 */

   @Override
   public boolean isSeekable() {
      return false;
   }

}