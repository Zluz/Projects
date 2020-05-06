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

	CHANCE_FLURRIES( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chanceflurries.png" ) ),
	CHANCE_RAIN( "scattered showers", "chance rain showers", 
			"occasional light rain", "$slight chance rain .*",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancerain.png" ) ),
	CHANCE_SLEET( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancesleet.png" ) ),
	CHANCE_SNOW( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/snow.png" ) ),
	CHANCE_TSTORMS(	"chance tstorms", "scattered thunderstorms",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancetstorms.png" ) ),
	
	CLEAR( 	"breezy", "mostlyclear", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/clear.png" ) ),
	CLOUDY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/cloudy.png" ) ),
	FLURRIES( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/flurries.png" ) ),
	FOG( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/fog.png" ) ),
	HAZY( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/hazy.png" ) ),
	
	MOSTLYCLOUDY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/mostlycloudy.png" ) ),
	MOSTLYSUNNY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/mostlysunny.png" ) ),
	
	PARTLYCLOUDY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/partlycloudy.png" ) ),
					
	PARTLYSUNNY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/partlysunny.png" ) ),
	RAIN( 	"showers", "heavy rain", "rain showers",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/rain.png" ) ),
	
	SLEET( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/sleet.png" ) ),
	SNOW( 	"snow showers", "rain and snow", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/snow.png" ) ),
	
	SUNNY(	"mostly sunny", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/sunny.png" ) ),
	
	TSTORMS( "thunderstorms", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/tstorms.png" ) ),
	
	UNKNOWN( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/unknown.png" ) ),

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
	
	private WeatherSymbol( final String... aliases ) {
		this.arrAliases = aliases;
		listMatches = new ArrayList<>();
		
		String strName = this.name().toLowerCase();
		strName = strName.replaceAll( "_", "" );
		listMatches.add( strName );
		for ( String strAlias : aliases ) {
			if ( ! strAlias.startsWith( "$" ) ) {
				strAlias = strAlias.trim().toLowerCase();
				strAlias = strAlias.replaceAll( " ", "" );
				strAlias = strAlias.replaceAll( "_", "" );
			}
			listMatches.add( strAlias );
		}
	}
	
	
	public static WeatherSymbol getSymbol( final String strText ) {
		if ( null==strText ) return WeatherSymbol.UNKNOWN;

		final String strTrimmed = strText.trim().toLowerCase();
		String strNormal = strTrimmed;
		strNormal = strNormal.replaceAll( " ", "" );
		strNormal = strNormal.replaceAll( "_", "" );

		for ( final WeatherSymbol symbol : WeatherSymbol.values() ) {
			for ( final String strMatch : symbol.listMatches ) {
				if ( ! strMatch.startsWith( "$" ) ) {
					if ( strNormal.equals( strMatch ) ) {
						return symbol;
					}
				}
			}
		}
		for ( final WeatherSymbol symbol : WeatherSymbol.values() ) {
			for ( String strMatch : symbol.listMatches ) {
				if ( strMatch.startsWith( "$" ) ) {
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
	
	
	public Image getIcon() {
		if ( null==imageIcon ) {
			try {
				for ( final String strAlias : this.arrAliases ) {
					if ( setNotFound.contains( strAlias ) ) {
						// already determined to not exist
					} else if ( strAlias.contains( ".png" ) ) {
						final File filePNG = new File( strAlias );
						if ( filePNG.isFile() ) {
					        imageIcon = new Image( Display.getCurrent(), 
					        				filePNG.getAbsolutePath() );
					        return imageIcon;
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
		}
		return imageIcon;
	}
	

	public static void main( final String[] args ) throws Exception {
		final Image image = WeatherSymbol.MOSTLYSUNNY.getIcon();
		System.out.println( "Image: " + image );
	}

	
	
}
