package org.cytoscape.app.communitydetection.edge;

import java.util.Map;

import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;

public class EdgeListReaderWriterTaskFactory {

	// ID of the CX writer service
	private static final String EDGE_READER_ID = "edgeListReaderFactory";
	private static final String EDGE_WRITER_ID = "edgeListNetworkWriterFactory";
	private static final String ID_TAG = "id";

	private CyNetworkViewWriterFactory writerFactory;
	private InputStreamTaskFactory readerFactory;


	private EdgeListReaderWriterTaskFactory() {
	}

	private static class SingletonHelper {

		private static final EdgeListReaderWriterTaskFactory INSTANCE = new EdgeListReaderWriterTaskFactory();
	}

	public static EdgeListReaderWriterTaskFactory getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public InputStreamTaskFactory getEdgeListReaderFactory() {
		return readerFactory;
	}

	public CyNetworkViewWriterFactory getEdgeListWriterFactory() {
		return writerFactory;
	}

	@SuppressWarnings("rawtypes")
	public void addWriterFactory(final CyNetworkViewWriterFactory factory, final Map properties) {
		final String id = (String) properties.get(ID_TAG);
		if (id != null && id.equals(EDGE_WRITER_ID)) {
			writerFactory = factory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeWriterFactory(final CyNetworkViewWriterFactory factory, Map properties) {
		final String id = (String) properties.get(ID_TAG);

		if (id != null && id.equals(EDGE_WRITER_ID)) {
			writerFactory = null;
		}
	}

	@SuppressWarnings("rawtypes")
	public void addReaderFactory(final InputStreamTaskFactory factory, final Map properties) {
		final String id = (String) properties.get(ID_TAG);
		if (id != null && id.equals(EDGE_READER_ID)) {
			readerFactory = factory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void removeReaderFactory(final InputStreamTaskFactory factory, Map properties) {
		final String id = (String) properties.get(ID_TAG);

		if (id != null && id.equals(EDGE_READER_ID)) {
			readerFactory = null;
		}
	}
}