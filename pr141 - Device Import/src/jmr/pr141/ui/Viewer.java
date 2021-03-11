package jmr.pr141.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import jmr.pr141.DeviceProvider;
import jmr.pr141.DeviceReference;
import jmr.pr141.DeviceService;
import jmr.pr141.device.Device;
import jmr.pr141.device.Device.TextProperty;


public class Viewer {

	final static Display display = Display.getDefault();

	final private Shell shell;
	final private Composite comp;
	
	final private static String[] arrBoolOptions = 
					new String[] { "<null>", "YES", "NO" };

	
	final Text txtSearchTAC;
	final Button btnSearchTAC;
	final Text txtStatus;
	
	final Combo cmbDeviceSources;
	final Combo cmbReference;
	
	final Text txtTAC;
	final Text txtSimCount;
	final Combo cmbWLAN;
	final Combo cmbBluetooth;
	final Text txtCountryCode;
	final Canvas canvasImage;
	
	Image imageThumbnail;
	String strImageNote;
	
	
	final EnumMap<TextProperty,Text> mapText = 
							new EnumMap<>( TextProperty.class );
	
	final DeviceService devices = new DeviceService();


	public Viewer() {
		this.shell = new Shell( display, SWT.SHELL_TRIM );
		shell.setText( "Device Viewer" );
		shell.setLayout( new FillLayout() );
		
		comp = new Composite( shell, SWT.RESIZE );
		
		comp.setLayout( new GridLayout( 3, false ) );
		
		txtSearchTAC = new Text( comp, SWT.BORDER );
		final GridData gdSearchTAC = new GridData();
		gdSearchTAC.horizontalSpan = 2;
		gdSearchTAC.grabExcessHorizontalSpace = true;
		gdSearchTAC.horizontalAlignment = GridData.FILL;
		txtSearchTAC.setLayoutData( gdSearchTAC );
		btnSearchTAC = new Button( comp, SWT.PUSH );
		btnSearchTAC.setText( "Search for TAC" );
		btnSearchTAC.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected( final SelectionEvent e ) {
				Viewer.this.doSearchForTAC();
			}
		});
		
		txtStatus = new Text( comp, SWT.READ_ONLY );
		final GridData gdStatus = new GridData();
		gdStatus.horizontalSpan = 3;
		gdStatus.grabExcessHorizontalSpace = true;
		gdStatus.horizontalAlignment = GridData.FILL;
		txtStatus.setLayoutData( gdStatus );

		new Label( comp, SWT.NONE ).setText( "Source" );
		cmbDeviceSources = new Combo( comp, SWT.DROP_DOWN | SWT.READ_ONLY );
		final GridData gdSources = new GridData();
		gdSources.horizontalSpan = 2;
		gdSources.grabExcessHorizontalSpace = true;
		gdSources.horizontalAlignment = GridData.FILL;
		cmbDeviceSources.setLayoutData( gdSources );

		new Label( comp, SWT.NONE ).setText( "Reference" );
		cmbReference = new Combo( comp, SWT.DROP_DOWN | SWT.READ_ONLY );
		cmbReference.setLayoutData( gdSources );
		
		new Label( comp, SWT.NONE ).setText( "TAC(s)" );
		final int iTACStyle = SWT.MULTI | SWT.BORDER | 
							SWT.V_SCROLL | SWT.H_SCROLL;
		txtTAC = new Text( comp, iTACStyle );
		final GridData gdTAC = new GridData();
		gdTAC.widthHint = 100;
		gdTAC.grabExcessVerticalSpace = true;
		gdTAC.horizontalAlignment = GridData.FILL;
		gdTAC.verticalAlignment = GridData.FILL;
		txtTAC.setLayoutData( gdTAC );
		txtTAC.setText( Text.DELIMITER + Text.DELIMITER + Text.DELIMITER
			  		+ Text.DELIMITER + Text.DELIMITER + Text.DELIMITER );
		
		final GridData gdImage = new GridData();
		gdImage.verticalAlignment = GridData.FILL;
		gdImage.horizontalAlignment = GridData.FILL;
		gdImage.grabExcessHorizontalSpace = true;
		gdImage.verticalSpan = 7;
		gdImage.widthHint = 200;
		gdImage.heightHint = 300;
		canvasImage = new Canvas( comp, SWT.BORDER );
		canvasImage.setLayoutData( gdImage );

		final GridData gdField = new GridData();
		gdField.horizontalAlignment = GridData.FILL;
		
		addTextProperty( comp, gdField, TextProperty.DEVICE_TYPE );
		addTextProperty( comp, gdField, TextProperty.OPERATING_SYSTEM );

		new Label( comp, SWT.NONE ).setText( "Sim Count" );
		txtSimCount = new Text( comp, SWT.SINGLE | SWT.BORDER );
		txtSimCount.setLayoutData( gdField );

		new Label( comp, SWT.NONE ).setText( "Bluetooth?" );
		cmbBluetooth = new Combo( comp, SWT.DROP_DOWN | SWT.READ_ONLY );
		cmbBluetooth.setItems( arrBoolOptions );

		new Label( comp, SWT.NONE ).setText( "WLAN?" );
		cmbWLAN = new Combo( comp, SWT.DROP_DOWN | SWT.READ_ONLY );
		cmbWLAN.setItems( arrBoolOptions );

		new Label( comp, SWT.NONE ).setText( "Country Code" );
		txtCountryCode = new Text( comp, SWT.SINGLE | SWT.BORDER );
		txtCountryCode.setLayoutData( gdField );
		
		final GridData gdWide = new GridData();
		gdWide.horizontalAlignment = GridData.FILL;
		gdWide.horizontalSpan = 2;

		final GridData gdBigHex = new GridData();
		gdBigHex.horizontalAlignment = GridData.FILL;
		gdBigHex.horizontalSpan = 2;
		gdBigHex.heightHint = 60;
		
		addTextProperty( comp, gdWide, TextProperty.MARKETING_NAME );
		addTextProperty( comp, gdWide, TextProperty.MANUFACTURER );
		addTextProperty( comp, gdWide, TextProperty.BRAND_NAME );
		addTextProperty( comp, gdWide, TextProperty.MODEL_NAME );
		
		addTextProperty( comp, gdWide, TextProperty.BANDS );
		addTextProperty( comp, gdWide, TextProperty.BANDS_5G );
		addTextProperty( comp, gdWide, TextProperty.RADIO_INTERFACE );
		addTextProperty( comp, gdWide, TextProperty.CHARACTERISTICS );
		
		final int iHexStyle = SWT.MULTI | SWT.BORDER | 
							SWT.V_SCROLL | SWT.WRAP;

		new Label( comp, SWT.NONE ).setText( 
								TextProperty.IMAGE_BASE64.getLabel() );
		final Text txtBase64 = new Text( comp, iHexStyle );
		txtBase64.setLayoutData( gdBigHex );
		this.mapText.put( TextProperty.IMAGE_BASE64, txtBase64 );

		shell.pack();
	}
	
	private void setStatus( final String strText ) {
		final String strSafe;
		if ( null != strText ) {
			strSafe = strText;
		} else {
			strSafe = "";
		}
		display.asyncExec( ()-> txtStatus.setText( strSafe ) );
	}
	
	private void addTextProperty( final Composite comp,
								  final GridData gd,
								  final TextProperty property ) {

		new Label( comp, SWT.NONE ).setText( property.getLabel() );
		final Text txt = new Text( comp, SWT.SINGLE | SWT.BORDER );
		this.mapText.put( property, txt );
		if ( null != gd ) {
			txt.setLayoutData( gd );
		}
	}
	
	public void open() {
		this.shell.open();
	}
	
	public Shell getShell() {
		return this.shell;
	}
	
	private void setTextProperty( final Device device,
								  final TextProperty property ) {
		if ( null == property ) return;
		if ( null == device ) return; // should clear the field
		
		final Text txt = this.mapText.get( property );
		if ( null == txt ) return;
		
		final String strValue = device.getProperty( property );
		if ( null == strValue ) {
			txt.setText( "<null>" );
		} else if ( strValue.length() > 100 ) {
			txt.setText( strValue.substring( 0, 100 ) + "..." );
		} else {
			txt.setText( strValue );
		}
	}
	
	
	private void setImageNote( final String strText ) {
		if ( null == strText ) {
			strImageNote = null;
		} else {
			final String strUI = strText.replace( "\n", Text.DELIMITER );
			this.strImageNote = strUI;
		}
		this.canvasImage.redraw();
	}
	
	
	public Image getImageFromBase64( final String strBase64 ) {
		final Base64.Decoder decoder = Base64.getDecoder();
		try {
			final byte[] arrBytes = decoder.decode( strBase64.trim() );
			final ByteArrayInputStream bais = new ByteArrayInputStream( arrBytes );
			final Image image = new Image( display, bais );
			setImageNote( null );
			return image;
			
		} catch ( final IllegalArgumentException e ) {
			// can happen if encoding is bad
			final String strException = e.toString().replace( ":", "\n" );
			setImageNote( "Failed to load image, encountered\n " 
						+ strException );
			return null;
		}
	}
	

	private void addProviders( final DeviceService devices ) {
		final List<DeviceProvider> list = devices.getAllDeviceProviders();
		for ( final DeviceProvider provider : list ) {
			final String strName = provider.getName();
			final List<String> listExisting = 
							Arrays.asList( cmbDeviceSources.getItems() );
			if ( ! listExisting.contains( strName ) ) {
				cmbDeviceSources.add( strName );
			}
		}
	}
	
	
	public void doSearchForTAC() {
		final long lTAC;
		try {
			final Long lCandidate = Long.parseLong( txtSearchTAC.getText() );
			lTAC = lCandidate.longValue();
		} catch ( final NumberFormatException e ) {
			this.setStatus( "Invalid TAC: " + e.toString() );
			return;
		}
		
		final List<DeviceReference> 
				listReferences = devices.getAllDeviceReferences( lTAC );

		this.load( listReferences, 0 );
	}
	
	
	public void loadSourceFile( final File file ) {
		this.devices.load( file );
		this.addProviders( devices );
	}
	
	private void setComboSelection( final Combo combo,
									final Boolean bValue ) {
		if ( null == bValue ) {
			combo.select( 0 );
		} else if ( bValue ) {
			combo.select( 1 );
		} else {
			combo.select( 2 );
		}
	}
	
	
	public void load( final List<DeviceReference> listReferences,
					  final int iSelect ) {
		final String strError;
		if ( null == listReferences ) {
			strError = "Data loaded is null";
		} else if ( listReferences.isEmpty() ) {
			strError = "No data found";
		} else if ( listReferences.size() <= iSelect ) {
			strError = "List of references is too small for selection";
		} else if ( iSelect < 0 ) {
			strError = "Invalid selection";
		} else {
			strError = null;
		}
		if ( null != strError ) {
			this.setStatus( strError );
			return;
		}
		
		final long lTimeStart = System.currentTimeMillis();
		
		final DeviceReference ref = listReferences.get( iSelect );
		
		final Device device = ref.resolve();
		
		final long lTimeEnd = System.currentTimeMillis();
		final long lElapsed = lTimeEnd - lTimeStart;
		this.setStatus( "Device loaded in " + lElapsed + " ms" );

		final String strProviderName = ref.getProvider().getName(); 
		final String[] arrSources = cmbDeviceSources.getItems();
		for ( int i = 0; i < arrSources.length; i++ ) {
			if ( strProviderName.equals( arrSources[ i ] ) ) {
				cmbDeviceSources.select( i );
			}
		}

		final int iRefCount = listReferences.size();
		final String[] arrReferences = new String[ iRefCount ];
		for ( int i = 0; i < iRefCount; i++ ) {
			final String strName = listReferences.get( i ).getName();
			arrReferences[ i ] = ""+ ( i + 1 ) + " : " + strName;
		}
		cmbReference.setItems( arrReferences );
		cmbReference.select( iSelect );
		
		setComboSelection( cmbWLAN, device.getWLAN() );
		setComboSelection( cmbBluetooth, device.getBluetooth() );
		
		final List<Long> listTACs = device.getTACs();
		final StringBuilder sbTACs = new StringBuilder();
		if ( null != listTACs ) {
			for ( final Long lTAC : listTACs ) {
				sbTACs.append( ""+ lTAC );
				sbTACs.append( Text.DELIMITER );
			}
			txtTAC.setText( sbTACs.toString() );
		} else {
			txtTAC.setText( "<null>" );
		}
		
		txtSimCount.setText( ""+ device.getSimCount() );
		
		for ( final TextProperty property : TextProperty.values() ) {
			setTextProperty( device, property );
		}
		
		final String strImageData = 
						device.getProperty( TextProperty.IMAGE_BASE64 );
		
		if ( null != imageThumbnail ) {
			imageThumbnail.dispose();
		}
		this.imageThumbnail = getImageFromBase64( strImageData );
		this.canvasImage.addPaintListener( event-> {
			if ( null != imageThumbnail ) {
				event.gc.drawImage( imageThumbnail, 0, 0 );
			}
			if ( null != strImageNote ) {
				event.gc.drawText( strImageNote, 10, 10 );
			}
		});
		
		this.canvasImage.redraw();
//		this.comp.setRedraw( true );
	}

	public static void main( final String[] args ) {

//		final String strFile = "/data/Development/CM/test.tar";
		final String strFile = "D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
							+ "catalog.tsv";
		final File file = new File( strFile );

		
		final Viewer ui = new Viewer();
		ui.open();
		
//		final String strB64 = "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ZQAyMDE3LTEyLTA4VDE1OjQ2OjA3KzAyOjAwLylk0gAAACV0RVh0ZGF0ZTptb2RpZnkAMjAxNy0xMi0wOFQxMjo0MjowMyswMjowMN6jXl8AAAAVdEVYdGV4aWY6Q29sb3JTcGFjZQA2NTUzNTN7AG4AAAAhdEVYdGV4aWY6RGF0ZVRpbWUAMjAxMTowNDoxNSAxMzo0NTowNyafz00AAAAYdEVYdGV4aWY6RXhpZkltYWdlTGVuZ3RoADEwMBptdAEAAAAXdEVYdGV4aWY6RXhpZkltYWdlV2lkdGgAMTAwh/LxEwAAABN0RVh0ZXhpZjpFeGlmT2Zmc2V0ADE1NglYGfsAAAAhdEVYdGV4aWY6U29mdHdhcmUAQWRvYmUgUGhvdG9zaG9wIDcuMB5km4kAAAAcdEVYdGV4aWY6dGh1bWJuYWlsOkNvbXByZXNzaW9uADb5ZXBXAAAAKHRFWHRleGlmOnRodW1ibmFpbDpKUEVHSW50ZXJjaGFuZ2VGb3JtYXQAMjk0fEceFgAAAC90RVh0ZXhpZjp0aHVtYm5haWw6SlBFR0ludGVyY2hhbmdlRm9ybWF0TGVuZ3RoADIyMjf1BMk6AAAAH3RFWHRleGlmOnRodW1ibmFpbDpSZXNvbHV0aW9uVW5pdAAyJUBe0wAAAB90RVh0ZXhpZjp0aHVtYm5haWw6WFJlc29sdXRpb24ANzIvMdqHGCwAAAAfdEVYdGV4aWY6dGh1bWJuYWlsOllSZXNvbHV0aW9uADcyLzF074m9AAAAS3RFWHR4YXBNTTpEb2N1bWVudElEAGFkb2JlOmRvY2lkOnBob3Rvc2hvcDo5YzEzYjkzYS02NzVkLTExZTAtOTFmYy1jYzMxYzNiNDM5NWUyaob8AAAAAElFTkSuQmCC10";
//		final Image image = ui.getImageFromBase64( strB64 );
//		ui.canvasImage.setBackgroundImage( image );
//		ui.canvasImage.addPaintListener( event-> {
//			event.gc.drawImage( image, 0, 0 );
//		});

		
		
//		final DeviceService devices = new DeviceService();
//		ui.devices.load( file );
//		ui.addProviders( devices );
		ui.loadSourceFile( file );
		
		
//		final long lTAC = 35888803; // TAC appears early: Acer beTouch E400
//		final long lTAC = 35160003; // very repeated TAC: Sony Ericsson K770
		final long lTAC = 1318400;  // many repeats, appears in first 1000
//		final List<Device> list = devices.getAllDeviceRecords( lTAC );
//		final List<DeviceReference> 
//							list = devices.getAllDeviceReferences( lTAC );
		
		ui.txtSearchTAC.setText( ""+ lTAC );
		ui.doSearchForTAC();

//		final Device device = list.get( 0 );
//		final DeviceReference device = list.get( 0 );
//		ui.load( list, 0 );

		
		
	    while ( ! ui.getShell().isDisposed()) {
	      if ( display.readAndDispatch()) {
	    	  display.sleep();
	      }
		}
		display.dispose(); 
	}
	
}
