        package com.m2049r.xmrwallet.util;

        import java.text.ParseException;
        import java.text.SimpleDateFormat;
        import java.util.Calendar;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.Map;
        import java.util.TimeZone;
        import java.util.concurrent.TimeUnit;

        public class RestoreHeight {
            static private RestoreHeight Singleton = null;

            static public RestoreHeight getInstance() {
                if (Singleton == null) {
                    synchronized (RestoreHeight.class) {
                        if (Singleton == null) {
                            Singleton = new RestoreHeight();
                        }
                    }
                }
                return Singleton;
            }

            private Map<String, Long> blockheight = new HashMap<>();

            RestoreHeight() {
                blockheight.put("2018-02-01", 1200L);
                blockheight.put("2018-03-01", 21975L);
                blockheight.put("2018-04-01", 43950L);
                blockheight.put("2018-05-01", 65925L);
                blockheight.put("2018-06-01", 87900L);
                blockheight.put("2018-07-01", 109875L);
                blockheight.put("2018-08-01", 131850L);
                blockheight.put("2018-09-01", 153825L);
                blockheight.put("2018-10-01", 175800L);
                blockheight.put("2018-11-01", 197775L);
            }

            public long getHeight(String date) {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
                parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                parser.setLenient(false);
                try {
                    return getHeight(parser.parse(date));
                } catch (ParseException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }

            public long getHeight(final Date date) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.set(Calendar.DST_OFFSET, 0);
                cal.setTime(date);
                cal.add(Calendar.DAY_OF_MONTH, -4); // give it some leeway
                if (cal.get(Calendar.YEAR) < 2014)
                    return 0;
                if ((cal.get(Calendar.YEAR) == 2014) && (cal.get(Calendar.MONTH) <= 3))
                    // before May 2014
                    return 0;

                Calendar query = (Calendar) cal.clone();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                String queryDate = formatter.format(date);

                cal.set(Calendar.DAY_OF_MONTH, 1);
                long prevTime = cal.getTimeInMillis();
                String prevDate = formatter.format(prevTime);
                // lookup blockheight at first of the month
                Long prevBc = blockheight.get(prevDate);
                if (prevBc == null) {
                    // if too recent, go back in time and find latest one we have
                    while (prevBc == null) {
                        cal.add(Calendar.MONTH, -1);
                        if (cal.get(Calendar.YEAR) < 2014) {
                            throw new IllegalStateException("endless loop looking for blockheight");
                        }
                        prevTime = cal.getTimeInMillis();
                        prevDate = formatter.format(prevTime);
                        prevBc = blockheight.get(prevDate);
                    }
                }
                long height = prevBc;
                // now we have a blockheight & a date ON or BEFORE the restore date requested
                if (queryDate.equals(prevDate)) return height;
                // see if we have a blockheight after this date
                cal.add(Calendar.MONTH, 1);
                long nextTime = cal.getTimeInMillis();
                String nextDate = formatter.format(nextTime);
                Long nextBc = blockheight.get(nextDate);
                if (nextBc != null) { // we have a range - interpolate the blockheight we are looking for
                    long diff = nextBc - prevBc;
                    long diffDays = TimeUnit.DAYS.convert(nextTime - prevTime, TimeUnit.MILLISECONDS);
                    long days = TimeUnit.DAYS.convert(query.getTimeInMillis() - prevTime,
                            TimeUnit.MILLISECONDS);
                    height = Math.round(prevBc + diff * (1.0 * days / diffDays));
                } else {
                    long days = TimeUnit.DAYS.convert(query.getTimeInMillis() - prevTime,
                            TimeUnit.MILLISECONDS);
                    height = Math.round(prevBc + 1.0 * days * (24 * 60 / 2));
                }
                return height;
            }
        }
