package writer;

import data.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static writer.DataFormat.ASCII;

public class UnstructuredGridXmlVtKWriter {
    private final UnstructuredGrid data;
    private final DataFormat format;
    private ByteOrder byteOrder = LITTLE_ENDIAN;

    /**
     * Writes unstructured data
     *
     * @param data   Unstructured data to be written
     * @param format "ascii" or "binary"
     */
    public UnstructuredGridXmlVtKWriter(UnstructuredGrid data, DataFormat format) {
        this.data = data;
        this.format = format;
    }

    void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public void write(File file) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        // <VTKFile type=”UnstructuredGrid” ...>
        Element vtkFile = doc.createElement("VTKFile");
        vtkFile.setAttribute("type", "UnstructuredGrid");
        vtkFile.setAttribute("version", "0.1");
        vtkFile.setAttribute("byte_order", byteOrder == LITTLE_ENDIAN ? "LittleEndian" : "BigEndian");

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

        if (data.cellScalarData != null) {
            for (ScalarData scalarData : data.cellScalarData) {
                Element dataArray = createDoubleDataArrayElement(scalarData.scalars, scalarData.dataName, doc);
                cellData.appendChild(dataArray);
            }
        }

        if (data.cellVectorData != null) {
            for (VectorData vectorData : data.cellVectorData) {
                Element dataArray = createVectorDataArrayElement(vectorData.vectors, vectorData.dataName, doc);
                cellData.appendChild(dataArray);
            }
        }

        return cellData;
    }

    private Element createPointDataElement(Document doc) {
        Element pointData = doc.createElement("PointData");
        if (data.pointScalarData != null) {
            for (ScalarData scalarData : data.pointScalarData) {
                Element dataArray = createDoubleDataArrayElement(scalarData.scalars, scalarData.dataName, doc);
                pointData.appendChild(dataArray);
            }
        }

        if (data.pointVectorData != null) {
            for (VectorData vectorData : data.pointVectorData) {
                Element dataArray = createVectorDataArrayElement(vectorData.vectors, vectorData.dataName, doc);
                pointData.appendChild(dataArray);
            }
        }

        return pointData;
    }

    private Element createCellsElement(Document doc) {
        Element cells = doc.createElement("Cells");

        // <DataArray type=”Int32” Name=”connectivity” .../>
        int[] connectivity = Arrays.stream(data.cells)
                .flatMapToInt(c -> Arrays.stream(c.connectivity))
                .toArray();
        Element dataArrayCellConn = createIntegerDataArrayElement(connectivity, "connectivity", doc);


        // <DataArray type=”Int32” Name=”offsets” .../>
        int[] offsets = new int[data.cells.length];
        offsets[0] = data.cells[0].connectivity.length;
        for (int i = 1; i < offsets.length; i++) {
            offsets[i] = data.cells[i].connectivity.length + offsets[i - 1];
        }
        Element dataArrayCellOffsets = createIntegerDataArrayElement(offsets, "offsets", doc);


        // <DataArray type=”UInt8” Name=”types” .../>
        byte[] types = new byte[data.cells.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = (byte) data.cells[i].vtkType.ID;
        }
        Element dataArrayCellTypes = createByteDataArrayElement(types, "types", doc);


        cells.appendChild(dataArrayCellConn);
        cells.appendChild(dataArrayCellOffsets);
        cells.appendChild(dataArrayCellTypes);

        return cells;
    }

    private Element createPointsElement(Document doc) {
        Element points = doc.createElement("Points");

        // <DataArray type="Float64" NumberOfComponents="3" ...">
        Element dataArrayPoints = createPointDataArrayElement(data.points, doc);

        points.appendChild(dataArrayPoints);

        return points;
    }

    private void writeToFile(Element root, File file) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult streamResult = new StreamResult(file);
        transformer.transform(new DOMSource(root), streamResult);
    }

    private Element createIntegerDataArrayElement(int[] intList, String name, Document doc) {
        String nodeText;
        if (format == ASCII) {
            nodeText = Arrays.stream(intList)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" "));
        } else {
            int size = intList.length * Integer.BYTES;
            ByteBuffer buffer = newByteBuffer(size);
            for (int i : intList) {
                buffer.putInt(i);
            }
            nodeText = encode(buffer.array());
        }

        Element intsDataArrayElement = doc.createElement("DataArray");
        intsDataArrayElement.setAttribute("type", "Int32");
        intsDataArrayElement.setAttribute("Name", name);
        intsDataArrayElement.setAttribute("format", format.toString());

        intsDataArrayElement.appendChild(doc.createTextNode(nodeText));

        return intsDataArrayElement;
    }

    private Element createByteDataArrayElement(byte[] byteList, String name, Document doc) {
        String nodeText;
        if (format == ASCII) {
            nodeText = IntStream.range(0, byteList.length)
                    .mapToObj(idx -> "" + byteList[idx])
                    .collect(Collectors.joining(" "));
        } else {
            int size = byteList.length * Byte.BYTES;
            ByteBuffer buffer = newByteBuffer(size);
            for (byte b : byteList) {
                buffer.put(b);
            }
            nodeText = encode(buffer.array());
        }

        Element bytesDataArrayElement = doc.createElement("DataArray");
        bytesDataArrayElement.setAttribute("type", "UInt8");
        bytesDataArrayElement.setAttribute("Name", name);
        bytesDataArrayElement.setAttribute("format", format.toString());

        bytesDataArrayElement.appendChild(doc.createTextNode(nodeText));

        return bytesDataArrayElement;
    }

    private Element createDoubleDataArrayElement(double[] doubleList, String name, Document doc) {
        String nodeText;
        if (format == ASCII) {
            nodeText = Arrays.stream(doubleList)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" "));
        } else {
            int size = doubleList.length * Double.BYTES;
            ByteBuffer buffer = newByteBuffer(size);
            for (Double d : doubleList) {
                buffer.putDouble(d);
            }
            nodeText = encode(buffer.array());
        }

        Element doublesDataArrayElement = doc.createElement("DataArray");
        doublesDataArrayElement.setAttribute("type", "Float64");
        doublesDataArrayElement.setAttribute("Name", name);
        doublesDataArrayElement.setAttribute("format", format.toString());

        doublesDataArrayElement.appendChild(doc.createTextNode(nodeText));

        return doublesDataArrayElement;
    }

    private Element createVectorDataArrayElement(Vector[] vectorList, String name, Document doc) {
        String nodeText;
        if (format == ASCII) {
            nodeText = Arrays.stream(vectorList)
                    .map(v -> v.x + " " + v.y + " " + v.z)
                    .collect(Collectors.joining(" "));
        } else {
            int size = vectorList.length * Double.BYTES * 3;
            ByteBuffer buffer = newByteBuffer(size);
            for (Vector v : vectorList) {
                buffer.putDouble(v.x);
                buffer.putDouble(v.y);
                buffer.putDouble(v.z);
            }
            nodeText = encode(buffer.array());
        }

        Element vectorsDataArrayElement = doc.createElement("DataArray");
        vectorsDataArrayElement.setAttribute("type", "Float64");
        vectorsDataArrayElement.setAttribute("Name", name);
        vectorsDataArrayElement.setAttribute("format", format.toString());
        vectorsDataArrayElement.setAttribute("NumberOfComponents", "3");

        vectorsDataArrayElement.appendChild(doc.createTextNode(nodeText));

        return vectorsDataArrayElement;
    }

    private Element createPointDataArrayElement(Point[] pointList, Document doc) {
        String nodeText;
        if (format == ASCII) {
            nodeText = Arrays.stream(pointList)
                    .map(p -> p.x + " " + p.y + " " + p.z)
                    .collect(Collectors.joining(" "));
        } else {
            int size = pointList.length * Double.BYTES * 3;
            ByteBuffer buffer = newByteBuffer(size);
            for (Point p : pointList) {
                buffer.putDouble(p.x);
                buffer.putDouble(p.y);
                buffer.putDouble(p.z);
            }
            nodeText = encode(buffer.array());
        }

        Element pointsDataArrayElement = doc.createElement("DataArray");
        pointsDataArrayElement.setAttribute("type", "Float64");
        pointsDataArrayElement.setAttribute("format", format.toString());
        pointsDataArrayElement.setAttribute("NumberOfComponents", "3");

        pointsDataArrayElement.appendChild(doc.createTextNode(nodeText));

        return pointsDataArrayElement;
    }

    private ByteBuffer newByteBuffer(int size) {
        return ByteBuffer.allocate(size).order(byteOrder);
    }

    private String encode(byte[] dataBytes) {
        byte[] headerBytes = newByteBuffer(Integer.BYTES).putInt(dataBytes.length).array();
        String header = Base64.getEncoder().encodeToString(headerBytes);
        String data = Base64.getEncoder().encodeToString(dataBytes);

        return header + data;
    }
}
