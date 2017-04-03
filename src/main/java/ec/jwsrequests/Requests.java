/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.jwsrequests;

import ec.demetra.xml.core.XmlTs;
import ec.demetra.xml.processing.XmlProcessingContext;
import ec.demetra.xml.sa.tramoseats.XmlTramoSeatsAtomicRequest;
import ec.demetra.xml.sa.tramoseats.XmlTramoSeatsRequests;
import ec.demetra.xml.sa.tramoseats.XmlTramoSeatsSpecification;
import ec.demetra.xml.sa.x13.XmlX13AtomicRequest;
import ec.demetra.xml.sa.x13.XmlX13Requests;
import ec.demetra.xml.sa.x13.XmlX13Specification;
import ec.jwsacruncher.WsaConfig;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.algorithm.ProcessingContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Requests {

    static final JAXBContext XML_X13REQUESTS, XML_TRAMOSEATSREQUESTS;

    static {
        try {
            XML_TRAMOSEATSREQUESTS = JAXBContext.newInstance(XmlTramoSeatsRequests.class);
            XML_X13REQUESTS = JAXBContext.newInstance(XmlX13Requests.class);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static XmlX13Requests createX13Requests(WsaConfig config, SaProcessing processing, ProcessingContext context) {
        int n = 0;
        XmlX13Requests x13r = new XmlX13Requests();
        for (SaItem item : processing) {
            if (item.getEstimationSpecification() instanceof X13Specification) {
                ++n;
                XmlX13AtomicRequest xcur = new XmlX13AtomicRequest();
                XmlX13Specification xspec = new XmlX13Specification();
                XmlX13Specification.MARSHALLER.marshal((X13Specification) item.getEstimationSpecification(), xspec);
                xcur.setSpecification(xspec);
                XmlTs xts = new XmlTs();
                XmlTs.MARSHALLER.marshal(item.getTs(), xts);
                xcur.setSeries(xts);
                x13r.getItems().add(xcur);
            }
        }
        if (n == 0) {
            return null;
        }
        x13r.setContext(context(context));
        fillFilter(config, x13r.getOutputFilter());
        return x13r;
    }

    public static XmlTramoSeatsRequests createTramoSeatsRequests(WsaConfig config, SaProcessing processing, ProcessingContext context) {
        int n = 0;
        XmlTramoSeatsRequests tsr = new XmlTramoSeatsRequests();
        for (SaItem item : processing) {
            if (item.getEstimationSpecification() instanceof TramoSeatsSpecification) {
                ++n;
                XmlTramoSeatsAtomicRequest xcur = new XmlTramoSeatsAtomicRequest();
                XmlTramoSeatsSpecification xspec = new XmlTramoSeatsSpecification();
                XmlTramoSeatsSpecification.MARSHALLER.marshal((TramoSeatsSpecification) item.getEstimationSpecification(), xspec);
                xcur.setSpecification(xspec);
                XmlTs xts = new XmlTs();
                XmlTs.MARSHALLER.marshal(item.getTs(), xts);
                xcur.setSeries(xts);
                tsr.getItems().add(xcur);
            }
        }
        if (n == 0) {
            return null;
        }
        tsr.setContext(context(context));
        fillFilter(config, tsr.getOutputFilter());
        return tsr;
    }

    private static void fillFilter(WsaConfig config, List<String> filter) {
        if (config.Matrix != null) {
            filter.addAll(Arrays.asList(config.Matrix));
        }
        if (config.TSMatrix != null) {
            filter.addAll(Arrays.asList(config.TSMatrix));
        }
    }

    private static XmlProcessingContext context(ProcessingContext context) {
        XmlProcessingContext xctx = new XmlProcessingContext();
        XmlProcessingContext.MARSHALLER.marshal(context, xctx);
        return xctx;
    }

    public static <T, X extends IXmlConverter<T>> boolean writeX13(Path sfile, XmlX13Requests requests) {
        File file = sfile.toFile();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            //XMLOutputFactory factory=XMLOutputFactory.newInstance();
            try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

                Marshaller marshaller = XML_X13REQUESTS.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(requests, writer);
                writer.flush();
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public static <T, X extends IXmlConverter<T>> boolean writeTramoSeats(Path sfile, XmlTramoSeatsRequests requests) {
        File file = sfile.toFile();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            //XMLOutputFactory factory=XMLOutputFactory.newInstance();
            try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {

                Marshaller marshaller = XML_TRAMOSEATSREQUESTS.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(requests, writer);
                writer.flush();
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }
}
