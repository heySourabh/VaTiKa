package writer;

import data.ScalarData;
import data.UnstructuredGrid;
import data.VectorData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UnstructuredGridXmlVtKWriter {
    private final UnstructuredGrid data;

    public UnstructuredGridXmlVtKWriter(UnstructuredGrid data) {
        this.data = data;
    }

    public void write(File file) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        // <VTKFile type=”UnstructuredGrid” ...>
        Element vtkFile = doc.createElement("VTKFile");
        vtkFile.setAttribute("type", "UnstructuredGrid");
        vtkFile.setAttribute("version", "0.1");
        vtkFile.setAttribute("byte_order", "LittleEndian");

        // <UnstructuredGrid>
        Element unstructuredGrid = doc.createElement("UnstructuredGrid");
        vtkFile.appendChild(unstructuredGrid);

        // <Piece NumberOfPoints=”#” NumberOfCells=”#”>
        Element piece = doc.createElement("Piece");
        piece.setAttribute("NumberOfPoints", data.points.length + "");
        piece.setAttribute("NumberOfCells", data.cells.length + "");
        unstructuredGrid.appendChild(piece);

        // <PointData>...</PointData>
        Element pointData = createPointDataElement(doc);
        piece.appendChild(pointData);

        // <CellData>...</CellData>
        Element cellData = createCellDataElement(doc);
        piece.appendChild(cellData);

        // <Points>...</Points>
        Element points = createPointsElement(doc);
        piece.appendChild(points);

        // <Cells>...</Cells>
        Element cells = createCellsElement(doc);
        piece.appendChild(cells);

        writeToFile(vtkFile, file);
    }

    private Element createCellDataElement(Document doc) {
        Element cellData = doc.createElement("CellData");

        for (ScalarData scalarData : data.cellScalarData) {
            Element dataArray = createScalarDataArrayElement(scalarData, doc);
            cellData.appendChild(dataArray);
        }

        for (VectorData vectorData : data.cellVectorData) {
            Element dataArray = createVectorDataArrayElement(vectorData, doc);
            cellData.appendChild(dataArray);
        }

        return cellData;
    }

    private Element createPointDataElement(Document doc) {
        Element pointData = doc.createElement("PointData");

        for (ScalarData scalarData : data.pointScalarData) {
            Element dataArray = createScalarDataArrayElement(scalarData, doc);
            pointData.appendChild(dataArray);
        }

        for (VectorData vectorData : data.pointVectorData) {
            Element dataArray = createVectorDataArrayElement(vectorData, doc);
            pointData.appendChild(dataArray);
        }

        return pointData;
    }

    private Element createVectorDataArrayElement(VectorData vectorData, Document doc) {
        Element vectors = doc.createElement("DataArray");
        vectors.setAttribute("type", "Float64");
        vectors.setAttribute("Name", vectorData.dataName);
        vectors.setAttribute("NumberOfComponents", "3");
        vectors.setAttribute("format", "ascii");

        String nodeText = Arrays.stream(vectorData.vectors)
                .map(v -> v.x + " " + v.y + " " + v.z)
                .collect(Collectors.joining(" "));

        Node node = doc.createTextNode(nodeText);
        vectors.appendChild(node);

        return vectors;
    }

    private Element createScalarDataArrayElement(ScalarData scalarData, Document doc) {
        Element scalars = doc.createElement("DataArray");
        scalars.setAttribute("type", "Float64");
        scalars.setAttribute("Name", scalarData.dataName);
        scalars.setAttribute("format", "ascii");

        String nodeText = Arrays.stream(scalarData.scalars)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(" "));

        Node node = doc.createTextNode(nodeText);
        scalars.appendChild(node);

        return scalars;
    }

    private Element createCellsElement(Document doc) {
        Element cells = doc.createElement("Cells");

        // <DataArray type=”Int32” Name=”connectivity” .../>
        Element dataArrayCellConn = doc.createElement("DataArray");
        dataArrayCellConn.setAttribute("type", "Int32");
        dataArrayCellConn.setAttribute("Name", "connectivity");
        dataArrayCellConn.setAttribute("format", "ascii");
        dataArrayCellConn.appendChild(cellConnectivityToNode(doc));

        // <DataArray type=”Int32” Name=”offsets” .../>
        Element dataArrayCellOffsets = doc.createElement("DataArray");
        dataArrayCellOffsets.setAttribute("type", "Int32");
        dataArrayCellOffsets.setAttribute("Name", "offsets");
        dataArrayCellOffsets.setAttribute("format", "ascii");
        dataArrayCellOffsets.appendChild(cellOffsetsToNode(doc));

        // <DataArray type=”UInt8” Name=”types” .../>
        Element dataArrayCellTypes = doc.createElement("DataArray");
        dataArrayCellTypes.setAttribute("type", "Int8");
        dataArrayCellTypes.setAttribute("Name", "types");
        dataArrayCellTypes.setAttribute("format", "ascii");
        dataArrayCellTypes.appendChild(cellTypesToNode(doc));

        cells.appendChild(dataArrayCellConn);
        cells.appendChild(dataArrayCellOffsets);
        cells.appendChild(dataArrayCellTypes);

        return cells;
    }

    private Element createPointsElement(Document doc) {
        Element points = doc.createElement("Points");
        // <DataArray type="Float32" NumberOfComponents="3" format="ascii">
        Element dataArrayPoints = doc.createElement("DataArray");
        dataArrayPoints.setAttribute("type", "Float64");
        dataArrayPoints.setAttribute("NumberOfComponents", "3");
        dataArrayPoints.setAttribute("format", "ascii");
        dataArrayPoints.appendChild(pointsToNode(doc));

        points.appendChild(dataArrayPoints);

        return points;
    }

    private void writeToFile(Element root, File file) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult streamResult = new StreamResult(file);
        transformer.transform(new DOMSource(root), streamResult);
    }

    private Node cellTypesToNode(Document doc) {
        String nodeText = Arrays.stream(data.cells)
                .map(c -> c.vtkType)
                .map(v -> v.ID + "")
                .collect(Collectors.joining(" "));

        return doc.createTextNode(nodeText);
    }

    private Node cellOffsetsToNode(Document doc) {
        int[] offsets = new int[data.cells.length];
        offsets[0] = data.cells[0].connectivity.length;
        for (int i = 1; i < offsets.length; i++) {
            offsets[i] = data.cells[i].connectivity.length + offsets[i - 1];
        }
        String nodeText = Arrays.stream(offsets)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(" "));

        return doc.createTextNode(nodeText);
    }

    private Node cellConnectivityToNode(Document doc) {
        String nodeText = Arrays.stream(data.cells)
                .flatMap(c -> Arrays.stream(c.connectivity).mapToObj(String::valueOf))
                .collect(Collectors.joining(" "));

        return doc.createTextNode(nodeText);
    }

    private Node pointsToNode(Document doc) {
        String nodeText = Arrays.stream(data.points)
                .map(p -> p.x + " " + p.y + " " + p.z)
                .collect(Collectors.joining(" "));

        return doc.createTextNode(nodeText);
    }
}
