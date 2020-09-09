import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ReadJson {

	public static void main( final String[] args ) {
		
//		final String strData = "b'{\"message\":{\"c1\":{\"systemInformation\":{\"criticalExtensions\":{\"systemInformation-r8\":{\"sib-TypeAndInfo\":[{\"sib5\":{\"interFreqCarrierFreqList\":[{\"dl-CarrierFreq\":5035,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":4,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":2300,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw100\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":6,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":1025,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":5,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"}],\"lateNonCriticalExtension\":\"D209862488A22020\"}}]}}}}}}'\r\n";
//		final String strData = "{\"message\":{\"c1\":{\"systemInformation\":{\"criticalExtensions\":{\"systemInformation-r8\":{\"sib-TypeAndInfo\":[{\"sib5\":{\"interFreqCarrierFreqList\":[{\"dl-CarrierFreq\":5035,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":4,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":2300,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw100\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":6,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":1025,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":5,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"}],\"lateNonCriticalExtension\":\"D209862488A22020\"}}]}}}}}}";
		
//		final String strData = "b'{\"message\":{\"c1\":{\"systemInformation\":{\"criticalExtensions\":{\"systemInformation-r8\":{\"sib-TypeAndInfo\":[{\"sib5\":{\"interFreqCarrierFreqList\":[{\"dl-CarrierFreq\":5035,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":4,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":2300,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw100\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":6,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":1025,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":5,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"}],\"lateNonCriticalExtension\":\"D209862488A22020\"}}]}}}}}}'";
		final String strData = "{\"message\":{\"c1\":{\"systemInformation\":{\"criticalExtensions\":{\"systemInformation-r8\":{\"sib-TypeAndInfo\":[{\"sib5\":{\"interFreqCarrierFreqList\":[{\"dl-CarrierFreq\":5035,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":4,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":2300,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw100\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":6,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"},{\"dl-CarrierFreq\":1025,\"q-RxLevMin\":-62,\"t-ReselectionEUTRA\":1,\"threshX-High\":2,\"threshX-Low\":0,\"allowedMeasBandwidth\":\"mbw25\",\"presenceAntennaPort1\":true,\"cellReselectionPriority\":5,\"neighCellConfig\":\"80\",\"q-OffsetFreq\":\"dB0\"}],\"lateNonCriticalExtension\":\"D209862488A22020\"}}]}}}}}}";
		
//		final Gson gson = new Gson();
		final JsonParser parser = new JsonParser();
		final JsonElement je = parser.parse( strData );
		
//		System.out.println( "Result: " + je.toString() );
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		System.out.println( gson.toJson( je ) );
		
	}
	
}
