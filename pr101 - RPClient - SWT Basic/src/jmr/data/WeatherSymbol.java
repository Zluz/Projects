package jmr.data;

import java.io.File;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import jmr.rpclient.S2Resource;

// see 
// https://peter.build/weather-underground-icons/
public enum WeatherSymbol {

	CHANCE_FLURRIES( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chanceflurries.png" ) ),
	CHANCE_RAIN( "scatteredshowers", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancerain.png" ) ),
	CHANCE_SLEET( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancesleet.png" ) ),
	CHANCE_SNOW( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/snow.png" ) ),
	CHANCE_TSTORMS(	"chancetstorms", "scatteredthunderstorms",
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/chancetstorms.png" ) ),
	
	CLEAR( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/clear.png" ) ),
	CLOUDY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/cloudy.png" ) ),
	FLURRIES( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/flurries.png" ) ),
	FOG( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/fog.png" ) ),
	HAZY( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/hazy.png" ) ),
	
	MOSTLYCLOUDY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/mostlycloudy.png" ) ),
	MOSTLYSUNNY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/mostlysunny.png" ) ),
	
	PARTLYCLOUDY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/partlycloudy.png" ) ),
					
	PARTLYSUNNY( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/partlysunny.png" ) ),
	RAIN( 	"showers", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/rain.png" ) ),
	
	SLEET( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/sleet.png" ) ),
	SNOW( 	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/snow.png" ) ),
	
	SUNNY(	S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/sunny.png" ) ),
	
	TSTORMS( "thunderstorms", 
			S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/tstorms.png" ) ),
	
	UNKNOWN( S2Resource.resolvePath( "S:/Resources/files/weather/wunderground/unknown.png" ) ),
		
//	SUNNY,
//	SNOW,
//	SLEET,
//	MOSTLY_SUNNY,
//	PARTLY_CLOUDY,
//	SCATTERED_THUNDERSTORMS,
//	THUNDERSTORMS,
	;
	
	final String[] arrAliases;
	
	private WeatherSymbol( final String... aliases ) {
		this.arrAliases = aliases;
	}
	
	
	public static WeatherSymbol getSymbol( final String strText ) {
		if ( null==strText ) return WeatherSymbol.UNKNOWN;
		String strNormal = strText.trim().toUpperCase();
		strNormal = strNormal.replaceAll( " ", "" );
		strNormal = strNormal.replaceAll( "_", "" );
		for ( final WeatherSymbol symbol : WeatherSymbol.values() ) {
			String strSymbolText = symbol.name();
			strSymbolText = strSymbolText.replaceAll( "_", "" );
			if ( strNormal.equals( strSymbolText ) ) {
				return symbol;
			}
			for ( final String strAlias : symbol.arrAliases ) {
				String strANorm = strAlias.trim().toUpperCase();
				if ( strANorm.equals( strNormal ) ) {
					return symbol;
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
	
	
	public Image getIcon() {
		if ( null==imageIcon ) {
			try {
				for ( final String strAlias : this.arrAliases ) {
					final File filePNG = new File( strAlias );
					if ( filePNG.isFile() ) {
				        imageIcon = new Image( Display.getCurrent(), 
				        				filePNG.getAbsolutePath() );
				        return imageIcon;
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
