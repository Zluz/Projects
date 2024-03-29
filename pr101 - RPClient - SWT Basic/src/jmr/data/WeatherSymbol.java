package jmr.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import jmr.rpclient.S2Resource;

// see 
// https://peter.build/weather-underground-icons/
public enum WeatherSymbol {

	// intended icons

	UNKNOWN( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/unknown.png" ) ),

	CLEAR_DAY(
			"^.+://api.weather.gov/icons/.+/day/skc[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 33.png" ) ),
	CLEAR_NIGHT(
			"^.+://api.weather.gov/icons/.+/night/skc[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 32.png" ) ),

	MOSTLYCLOUDY_DAY( 
			"^.+://api.weather.gov/icons/.+/day/bkn[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 29.png" ) ),
	MOSTLYCLOUDY_NIGHT( 
			"^.+://api.weather.gov/icons/.+/night/bkn[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 28.png" ) ),

	MOSTLYCLEAR_DAY( 
			"^.+://api.weather.gov/icons/.+/day/few[,?].*", 
			"^.+://api.weather.gov/icons/.+/day/sct[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/mostlysunny.png" ) ),
	MOSTLYCLEAR_NIGHT( 
			"^.+://api.weather.gov/icons/.+/night/few[,?].*", 
			"^.+://api.weather.gov/icons/.+/night/sct[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 30.png" ) ),

	TSTORMS_HIGH_DAY(
			"^.+://api.weather.gov/icons/.+/day/tsra_hi[,?].*", 
			"^.+://api.weather.gov/icons/.+/day/tsra_sct[,?].*", 
			"^.+://api.weather.gov/icons/.+/day/.+/tsra_hi[,?].*", 
			"^.+://api.weather.gov/icons/.+/day/.+/tsra_sct[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 38.png" ) ),
	TSTORMS_HIGH_NIGHT(
			"^.+://api.weather.gov/icons/.+/night/tsra_hi[,?].*", 
			"^.+://api.weather.gov/icons/.+/night/tsra_sct[,?].*", 
			"^.+://api.weather.gov/icons/.+/night/.+/tsra_hi[,?].*", 
			"^.+://api.weather.gov/icons/.+/night/.+/tsra_sct[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 48.png" ) ),
	TSTORMS(
			"^.+://api.weather.gov/icons/.+/tsra[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 4.png" ) ),
	
	RAIN( 
			"^.+://api.weather.gov/icons/.+/rain[,?].*", 
			"^.+://api.weather.gov/icons/.+/rain_showers[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/Artboard 10.png" ) ),

	MOSTLYSUNNY( 
			"^.+://api.weather.gov/icons/.+/sct[,?].*", 
			"^.+://api.weather.gov/icons/.+/wind_sct[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/mostlysunny.png" ) ),

	// fallback icons

	CLEAR( 	"breezy", "mostlyclear", 
			"^.+://api.weather.gov/icons/.+/skc[,?].*", 
//			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/clear.png" ) ),
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/Artboard 33.png" ) ),
	
	CLOUDY( 
			"^.+://api.weather.gov/icons/.+/ovc[,?].*", 
			"^.+://api.weather.gov/icons/.+/wind_ovc[,?].*", 
//			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/cloudy.png" ) ),
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/Artboard 27" ) ),

	// old icon set, but maybe worth keeping
	
	CHANCE_SNOW( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/snow.png" ) ),

	FOG( 	
			"^.+://api.weather.gov/icons/.+/fog[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/fog.png" ) ),

	HAZY( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/hazy.png" ) ),
	
	SNOW( 	
			"^.+://api.weather.gov/icons/.+/snow[,?].*", 
			"^.+://api.weather.gov/icons/.+/rain_snow[,?].*", 
			"^.+://api.weather.gov/icons/.+/blizzard[,?].*", 
			"snow showers", "rain and snow", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/snow.png" ) ),

	// obsolete icons 
	
	_CHANCE_FLURRIES( 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chanceflurries.png" ) ),
	_CHANCE_RAIN(
//			"^.+:\\/\\/api.weather.gov\\/icons\\/.+\\/rain_showers[,?].*",
			"^.+://api.weather.gov/icons/.+/rain_showers[,?].*",
			"scattered showers", //"chance rain showers", 
			"^slight chance rain.*", 
			"^.*rain showers.*", 
//			"^mostly cloudy then chance rain showers.*",
//			"^partly cloudy then chance rain showers.*",
			"^.*cloudy then .*rain showers.*",
			"occasional light rain", "^occasional light rain.*", "^occasional rain.*",  
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancerain.png" ) ),
	
	_CHANCE_SLEET( 
//			"^.+://api.weather.gov/icons/.+/sleet,.+",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancesleet.png" ) ),
	
	_CHANCE_TSTORMS(	
			"chance tstorms", "scattered thunderstorms",
			"^.+://api.weather.gov/icons/.+/tsra_hi[,?].*", 
			"^chance showers and thunderstorms",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancetstorms.png" ) ),
	
	_FLURRIES( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/flurries.png" ) ),
	
	_MOSTLYCLOUDY( 
			"^.+://api.weather.gov/icons/.+/bkn[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/mostlycloudy.png" ) ),
	
	_PARTLYCLOUDY( 
			"^.+://api.weather.gov/icons/.+/few[,?].*", 
			"^.+://api.weather.gov/icons/.+/wind_few[,?].*", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/partlycloudy.png" ) ),
					
	_PARTLYSUNNY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/partlysunny.png" ) ),
	_RAIN( 	
			"^.+://api.weather.gov/icons/.+/rain[,?].*", 
			"^.+://api.weather.gov/icons/.+/rain_showers[,?].*", 
			"showers", "heavy rain", "rain showers",
			"^rain showers likely.*",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/rain.png" ) 
//			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/rain.svg" ) 
			),
	
	_SLEET( 	
			"^.+://api.weather.gov/icons/.+/sleet[,?].*", 
			"^.+://api.weather.gov/icons/.+/snow_fzra[,?].*", 
			"^.+://api.weather.gov/icons/.+/rain_fzra[,?].*", // should this be RAIN? 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/sleet.png" ) ),
	
	_SUNNY(	
			"^.+://api.weather.gov/icons/.+/sunny[,?].*", 
			"mostly sunny", 
			"^.*frost then sunny",
			"^.*frost then mostly sunny",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/sunny.png" ) ),
	
	_TSTORMS( 
			"^.+://api.weather.gov/icons/.+/tsra[,?].*", 
			"^.+://api.weather.gov/icons/.+/tsra_sct[,?].*", 
			"thunderstorms", 
			"^showers and thunderstorms likely",
			"^.* and thunderstorms",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/tstorms.png" ) ),
	
	/* Query to find previously used weather names
			SELECT * 
			FROM s2db.prop
			WHERE 
				TRUE
			    AND (name = 'text')
			GROUP BY
				value
			ORDER BY
				value;
	*/
	
	
//	SUNNY,
//	SNOW,
//	SLEET,
//	MOSTLY_SUNNY,
//	PARTLY_CLOUDY,
//	SCATTERED_THUNDERSTORMS,
//	THUNDERSTORMS,
	;
	
	final String[] arrAliases;
	final List<String> listMatches;
	final boolean bOutdated;
	
	private WeatherSymbol( final String... aliases ) {
		this.arrAliases = aliases;
		listMatches = new ArrayList<>();
		
		String strName = this.name().toLowerCase();
		strName = strName.replaceAll( "_", "" );
		listMatches.add( strName );
		for ( String strAlias : aliases ) {
			if ( ! strAlias.startsWith( "^" ) ) {
				strAlias = strAlias.trim().toLowerCase();
				strAlias = strAlias.replaceAll( " ", "" );
				strAlias = strAlias.replaceAll( "_", "" );
			}
			listMatches.add( strAlias );
		}
		
		bOutdated = '_' == this.name().charAt( 0 );
	}
	
	
	public static WeatherSymbol getSymbol( 	final String strText,
											final String strIconURL ) {
		if ( null != strIconURL ) {
			for ( final WeatherSymbol symbol : WeatherSymbol.values() ) {
				for ( final String strMatch : symbol.listMatches ) {
					if ( strMatch.startsWith( "^" ) 
									&& strMatch.contains( ":" ) ) {
						if ( strIconURL.matches( strMatch ) ) {
							
//							System.out.println( "Matched(1): " + strMatch );
							return symbol;
						}
					}
				}
			}
		}

		if ( null==strText ) return WeatherSymbol.UNKNOWN;

		final String strTrimmed = strText.trim().toLowerCase();
		String strNormal = strTrimmed;
		strNormal = strNormal.replaceAll( " ", "" );
		strNormal = strNormal.replaceAll( "_", "" );

		for ( final WeatherSymbol symbol : WeatherSymbol.values() ) {
			for ( final String strMatch : symbol.listMatches ) {
				if ( ! strMatch.startsWith( "^" ) ) {
					if ( strNormal.equals( strMatch ) ) {
						return symbol;
					}
				}
			}
		}
		for ( final WeatherSymbol symbol : WeatherSymbol.values() ) {
			for ( String strMatch : symbol.listMatches ) {
				if ( strMatch.startsWith( "^" ) ) {
					strMatch = strMatch.substring( 1 ); // why?
					if ( strTrimmed.matches( strMatch ) ) {
						return symbol;
					}
				}
			}
		}
		return WeatherSymbol.UNKNOWN;
	}
	
	
//	// see
//	// https://xmlgraphics.apache.org/batik/using/transcoder.html
//	public Image getIcon( final int iSize ) {
//
//        // Create a JPEG transcoder
//        JPEGTranscoder t = new JPEGTranscoder();
//
//        // Set the transcoding hints.
//        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
//                   new Float(.8));
//
//        // Create the transcoder input.
//        String svgURI;
//		try {
//			svgURI = new File( this.strImageFile ).toURL().toString();
//			
//	        TranscoderInput input = new TranscoderInput(svgURI);
//
//	        // Create the transcoder output.
//	        final File fileJPG = File.createTempFile( this.name() + "_", ".jpg" );
//	        OutputStream ostream = new FileOutputStream( fileJPG );
//	        TranscoderOutput output = new TranscoderOutput(ostream);
//
//	        // Save the image.
//	        t.transcode(input, output);
//
//	        // Flush and close the stream.
//	        ostream.flush();
//	        ostream.close();
//	        
//	        final Image image = new Image( 
//	        			Display.getCurrent(), fileJPG.getAbsolutePath() );
//	        return image;
//
//		} catch ( final Exception e ) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}

	private Image imageIcon = null;
	
	
	private final static Set<String> setNotFound = new HashSet<>();
	
	
	public static Image getIconUnknown() {
		if ( null == WeatherSymbol.UNKNOWN.imageIcon ) {
			final File filePNG = new File( WeatherSymbol.UNKNOWN.arrAliases[0] );
			WeatherSymbol.UNKNOWN.imageIcon = 
									new Image( Display.getCurrent(), 
				    				filePNG.getAbsolutePath() );
		}
		return WeatherSymbol.UNKNOWN.imageIcon;
	}
	
	public Image getIcon() {
		if ( null==imageIcon ) {
			
			final Thread threadLoadIcon = new Thread() {
				public void run() {
					
					try {
						for ( final String strAlias : arrAliases ) {
							if ( setNotFound.contains( strAlias ) ) {
								// already determined to not exist
							} else if ( strAlias.contains( ".png" ) ) {
								final File filePNG = new File( strAlias );
								if ( filePNG.isFile() ) {
							        imageIcon = new Image( Display.getCurrent(), 
							        				filePNG.getAbsolutePath() );
//							        return imageIcon;
								} else {
									setNotFound.add( strAlias );
									System.out.println( "Weather symbol alias "
											+ "image not found: " + strAlias );
								}
							} else {
								setNotFound.add( strAlias );
							}
						}
					} catch ( final Exception e ) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			};
			threadLoadIcon.start();
			return getIconUnknown();
		}
		return imageIcon;
	}
	
	
	public boolean isOutdated() {
		return this.bOutdated;
	}
	

	public static void main( final String[] args ) throws Exception {
//		final Image image = WeatherSymbol.MOSTLYSUNNY.getIcon();
//		System.out.println( "Image: " + image );
		
		final String strURL = 
//				"https://api.weather.gov/icons/land/day/rain,40/rain,30?size=medium";
				"https://api.weather.gov/icons/land/day/rain,40/few,30?size=medium";
		WeatherSymbol.getSymbol( null, strURL );
	}

	
	
}
