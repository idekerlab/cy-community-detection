package org.cytoscape.app.communitydetection.hierarchy;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.cytoscape.app.communitydetection.event.BaseurlUpdatedEvent;
import org.cytoscape.app.communitydetection.util.AppUtils;
import org.cytoscape.app.communitydetection.util.ShowDialogUtil;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CustomParameter;

/**
 *
 * @author churas
 */
public class LauncherDialogTest {
	

	@Test
	public void testCreateGUIFirstCallNullArgsPassedToConstructor() throws Exception{
		
		LauncherDialog ld = new LauncherDialog(null, null, null, null,null);
		try {
			ld.createGUI(null);
			fail("Expected NullPointerException");
		} catch(NullPointerException npe){
			
		}
	}
	
	@Test
	public void testgetSelectedCommunityDetectionAlgorithmBeforeGuiLoaded() throws Exception{
		
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPES);
		assertNull(ld.getSelectedCommunityDetectionAlgorithm());
	}
	
	@Test
	public void testCreateGUIFirstCallSuccessCDAlgoNoCustomParams() throws Exception {
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		List<CommunityDetectionAlgorithm> cdaList = new ArrayList<>();
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		cda.setInputDataFormat(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		cda.setDescription("description");
		cda.setDisplayName("displayname");
		cdaList.add(cda);
		when(mockAlgoFac.getAlgorithms(mockComponent, AppUtils.CD_ALGORITHM_INPUT_TYPES, false)).thenReturn(cdaList);
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPES);
		assertTrue(ld.createGUI(mockComponent));
		Map<String, String> algoCustParams = ld.getAlgorithmCustomParameters("foo");
		assertEquals(0, algoCustParams.size());
	}
	
	@Test
	public void testCreateGUIFirstCallSuccessCDAlgoWithCustomParams() throws Exception {
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		List<CommunityDetectionAlgorithm> cdaList = new ArrayList<>();
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		cda.setInputDataFormat(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		cda.setDescription("description");
		cda.setDisplayName("displayname");
		HashSet<CustomParameter> custParams = new HashSet<>();
		CustomParameter custOne = new CustomParameter();
		custOne.setName("--custone");
		custOne.setDisplayName("custonedisplay");
		custOne.setDescription("custonedescription");
		custOne.setType("flag");
		custParams.add(custOne);
		CustomParameter custTwo = new CustomParameter();
		custTwo.setName("--custtwo");
		custTwo.setDisplayName("custtwodisplay");
		custTwo.setDescription("custtwodescription");
		custTwo.setType("value");
		custTwo.setDefaultValue("custtwodefault");
		custParams.add(custTwo);
		cda.setCustomParameters(custParams);
		cdaList.add(cda);
		
		when(mockAlgoFac.getAlgorithms(mockComponent, AppUtils.CD_ALGORITHM_INPUT_TYPES, false)).thenReturn(cdaList);
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPES);
		assertTrue(ld.createGUI(mockComponent));
		Map<String, String> algoCustParams = ld.getAlgorithmCustomParameters("foo");
		
		assertEquals(1, algoCustParams.size());	
		assertEquals("custtwodefault", algoCustParams.get("--custtwo"));
		CommunityDetectionAlgorithm selectedcda = ld.getSelectedCommunityDetectionAlgorithm();
		assertNotNull(cda);
	}
	
	@Test
	public void testCreateGUIFirstCallSuccessMultipleCreateGUICalls() throws Exception {
		Component mockComponent = mock(Component.class);
		ShowDialogUtil mockDialog = mock(ShowDialogUtil.class);
		AboutAlgorithmEditorPaneFactoryImpl mockAbout = mock(AboutAlgorithmEditorPaneFactoryImpl.class);
		CustomParameterHelpJEditorPaneFactoryImpl mockCustom = mock(CustomParameterHelpJEditorPaneFactoryImpl.class);
		LauncherDialogAlgorithmFactory mockAlgoFac = mock(LauncherDialogAlgorithmFactory.class);
		List<CommunityDetectionAlgorithm> cdaList = new ArrayList<>();
		CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
		cda.setName("foo");
		cda.setInputDataFormat(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		cda.setDescription("description");
		cda.setDisplayName("displayname");
		cdaList.add(cda);
		
		//This second algorithm list is returned after a refresh call
		// and is an easy way to verify refresh is calling new endpoint
		List<CommunityDetectionAlgorithm> cdaListTwo = new ArrayList<>();
		CommunityDetectionAlgorithm cdaTwo = new CommunityDetectionAlgorithm();
		cdaTwo.setName("foo");
		cdaTwo.setInputDataFormat(AppUtils.CD_ALGORITHM_INPUT_TYPE);
		cdaTwo.setDescription("description");
		cdaTwo.setDisplayName("displayname");
		cdaListTwo.add(cdaTwo);
		HashSet<CustomParameter> custParams = new HashSet<>();
		CustomParameter custTwo = new CustomParameter();
		custTwo.setName("--custtwo");
		custTwo.setDisplayName("custtwodisplay");
		custTwo.setDescription("custtwodescription");
		custTwo.setType("value");
		custTwo.setDefaultValue("custtwodefault");
		custParams.add(custTwo);
		cdaTwo.setCustomParameters(custParams);
		
		
		when(mockAlgoFac.getAlgorithms(mockComponent, null, true)).thenReturn(cdaListTwo);
		when(mockAlgoFac.getAlgorithms(mockComponent, AppUtils.CD_ALGORITHM_INPUT_TYPES, false)).thenReturn(cdaList, cdaListTwo);
		
		LauncherDialog ld = new LauncherDialog(mockAbout, mockCustom,
				mockAlgoFac, mockDialog,AppUtils.CD_ALGORITHM_INPUT_TYPES);
		assertTrue(ld.createGUI(mockComponent));
		Map<String, String> algoCustParams = ld.getAlgorithmCustomParameters("foo");
		assertEquals(0, algoCustParams.size());
		
		// try refresh no update to REST url
		assertTrue(ld.createGUI(mockComponent));
		algoCustParams = ld.getAlgorithmCustomParameters("foo");
		assertEquals(0, algoCustParams.size());
		
		//try refresh with CHANGED REST URL
		BaseurlUpdatedEvent event = new BaseurlUpdatedEvent("http://oldfoo", "http://newfoo");
		ld.urlUpdatedEvent(event);
		assertTrue(ld.createGUI(mockComponent));
		algoCustParams = ld.getAlgorithmCustomParameters("foo");
		assertEquals(1, algoCustParams.size());
	}
	
}
