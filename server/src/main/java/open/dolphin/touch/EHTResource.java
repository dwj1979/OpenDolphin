/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package open.dolphin.touch;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import open.dolphin.converter.NLaboModuleConverter;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.AttachmentModel;
import open.dolphin.infomodel.BundleDolphin;
import open.dolphin.infomodel.ChartEventModel;
import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.DrugInteractionModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.IStampTreeModel;
import open.dolphin.infomodel.InfoModel;
import open.dolphin.infomodel.InteractionCodeList;
import open.dolphin.infomodel.KarteNumber;
import open.dolphin.infomodel.LastDateCount;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.NLaboItem;
import open.dolphin.infomodel.NLaboModule;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PatientFreeDocumentModel;
import open.dolphin.infomodel.PatientMemoModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.infomodel.StampModel;
import open.dolphin.infomodel.VitalModel;
import open.dolphin.session.ChartEventServiceBean;
import open.dolphin.session.KarteServiceBean;
import open.dolphin.touch.converter.IAllergyModel;
import open.dolphin.touch.converter.IAttachmentModel;
import open.dolphin.touch.converter.IBundleModule;
import open.dolphin.touch.converter.IDocument;
import open.dolphin.touch.converter.IDocument2;
import open.dolphin.touch.converter.IFastDocInfo;
import open.dolphin.touch.converter.IKarteNumber;
import open.dolphin.touch.converter.ILaboGraphItem;
import open.dolphin.touch.converter.ILaboValue;
import open.dolphin.touch.converter.ILastDateCount;
import open.dolphin.touch.converter.IOSHelper;
import open.dolphin.touch.converter.IPatientMemoModel;
import open.dolphin.touch.converter.IPatientModel;
import open.dolphin.touch.converter.IPatientVisitModel;
import open.dolphin.touch.converter.IPhysicalModel;
import open.dolphin.touch.converter.IRegisteredDiagnosis;
import open.dolphin.touch.converter.ISendPackage;
import open.dolphin.touch.converter.ISendPackage2;
import open.dolphin.touch.converter.IVitalModel;
import open.dolphin.touch.session.EHTServiceBean;
import open.orca.rest.ORCAConnection;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author kazushi
 */
@Path("/10/eht")
public class EHTResource extends open.dolphin.rest.AbstractResource {
    
    private static final String QUERY_FACILITYID_BY_1001
            ="select kanritbl from tbl_syskanri where kanricd='1001'";
    
    private static final String MOBILE_KIND = "mobile.kind";
    private static final String MOBILE_ONOFF = "mobile.onoff";
    private static final String SERVER_VERSION = "server.version";
    private static final String DOLPHIN_FACILITYID = "dolphin.facilityId";
    private static final String JAMRI_CODE = "jamri.code";
    private static final String USE_AS_PVTSERVER = "useAsPVTServer";
    private static final String PVT_LISTEN_BINDIP = "pvt.listen.bindIP";
    private static final String PVT_LISTEN_PORT = "pvt.listen.port";
    private static final String PVT_LISTEN_ENCODING = "pvt.listen.encoding";
    private static final String CLAIM_CONN = "claim.conn";
    private static final String CLAIM_HOST = "claim.host";
    private static final String CLAIM_SEND_PORT = "claim.send.port";
    private static final String CLAIM_SEND_ENCODING = "claim.send.encoding";
    private static final String RP_DEFAULT_INOUT = "rp.default.inout";
    private static final String PVTLIST_CLEAR = "pvtlist.clear";
    private static final String CLAIM_JDBC_URL = "claim.jdbc.url";
    private static final String CLAIM_USER = "claim.user";
    private static final String CLAIM_PASSWORD = "claim.password";
    
    @Inject
    private EHTServiceBean ehtService;
    
    @Inject
    private KarteServiceBean karteService;
    
    @Inject
    private ChartEventServiceBean eventServiceBean;
    
    @Context
    private HttpServletRequest servletReq;

    
//minagawa^  Deploy 技術の問題で standalone.xml に ORCA DS は指定しない
    //@Resource(mappedName="java:jboss/datasources/OrcaDS")
    //private DataSource ds;
//minagawa$    
    
    @GET
    @Path("/patient/firstVisitors/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getFirstVisitors(final @PathParam("param") String param) {

        return new StreamingOutput() {
            
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                String [] params = param.split(",");

                if (params.length !=2) {
                    throw new WebApplicationException();
                }

                // 医療機関ID、最初の結果、最大件数
                String facilityId = getRemoteFacility(servletReq.getRemoteUser());
                int firstResult = Integer.parseInt(params[0]);
                int maxResult = Integer.parseInt(params[1]);

                // 新患リストを取得する
                List<PatientModel> list = ehtService.getFirstVisitors(facilityId, firstResult, maxResult);
                List<IPatientModel> result = new ArrayList(list.size());
                
                for (PatientModel patient : list) {
                    IPatientModel ipm = new IPatientModel();
                    ipm.setModel(patient);
                    result.add(ipm);
                }
                
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @GET
    @Path("/pvtList")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPvtList() {
        
        return new StreamingOutput() {
            
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
             
                String fid = getRemoteFacility(servletReq.getRemoteUser());
                List<PatientVisitModel> list = eventServiceBean.getPvtList(fid);
                
                List<IPatientVisitModel> result = new ArrayList(list.size());
                for (PatientVisitModel model : list) {
                    IPatientVisitModel ipv = new IPatientVisitModel();
                    ipv.setModel(model);
                    result.add(ipv);
                }
                
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @GET
    @Path("/patient/pvt/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPatientsByPvt(final @Context HttpServletRequest servletReq, final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
             
                String fid = getRemoteFacility(servletReq.getRemoteUser());
                String pvtDate = param;
                List<PatientModel> list = ehtService.getPatientsByPvtDate(fid, pvtDate);
                
                List<IPatientModel> result = new ArrayList(list.size());
                for (PatientModel model : list) {
                    IPatientModel ipv = new IPatientModel();
                    ipv.setModel(model);
                    result.add(ipv);
                }
                
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @GET
    @Path("/patient/documents/status")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPatientsByTmpKarte(final @Context HttpServletRequest servletReq) {
        
        return new StreamingOutput() {
            
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                String fid = getRemoteFacility(servletReq.getRemoteUser());
                
                List<PatientModel> list = ehtService.getTmpKarte(fid);
                
                List<IPatientModel> result = new ArrayList(list.size());
                for (PatientModel model : list) {
                    IPatientModel ipv = new IPatientModel();
                    ipv.setModel(model);
                    result.add(ipv);
                }
                
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @GET
    @Path("/lastDateCount/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getLastDateCount(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                String [] params = param.split(",");
                long ptPK = Long.parseLong(params[0]);
                StringBuilder sb = new StringBuilder();
                sb.append(params[1]).append(":").append(params[2]);
                String fidPid = sb.toString();
                LastDateCount data = ehtService.getLastDateCount(ptPK, fidPid);
                ILastDateCount result = new ILastDateCount();
                result.fromModel(data);
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @GET
    @Path("/memo/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPatientMemo(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long ptPK = Long.parseLong(param);
                PatientMemoModel memo = ehtService.getPatientMemo(ptPK);
                IPatientMemoModel conv = new IPatientMemoModel();
                if (memo!=null) {
                    conv.fromModel(memo);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, conv);
            }
        };
    }
    
    @POST
    @Path("/memo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput postPatientMemo(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                IPatientMemoModel model = mapper.readValue(json, IPatientMemoModel.class);
                int cnt = ehtService.addPatientMemo(model.toModel());
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    
    @PUT
    @Path("/memo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput putPatientMemo(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                IPatientMemoModel model = mapper.readValue(json, IPatientMemoModel.class);
                int cnt = ehtService.updatePatientMemo(model.toModel());
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
                
            }
        };
    }
    
    @DELETE
    @Path("/memo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput deletePatientMemo(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                IPatientMemoModel model = mapper.readValue(json, IPatientMemoModel.class);
                int cnt = ehtService.deletePatientMemo(model.toModel());
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    
//s.oh^ 2014/04/03/サマリー対応
    @GET
    @Path("/freedocument/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPatientFreeDocument(final @Context HttpServletRequest servletReq, final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                String fpid = getFidPid(servletReq.getRemoteUser(), param);
                PatientFreeDocumentModel pfdm = ehtService.getPatientFreeDocument(fpid);
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, pfdm.getComment());
            }
        };
    }
//s.oh$
    
    @GET
    @Path("/allergy/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getAllergies(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                long ptPK = Long.parseLong(param);
                List<AllergyModel> list = ehtService.getAllergies(ptPK);
                List<IAllergyModel> result = new ArrayList();
                for (AllergyModel m : list) {
                    IAllergyModel ac = new IAllergyModel();
                    ac.fromModel(m);
                    result.add(ac);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @POST
    @Path("/allergy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput postAllergies(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                IAllergyModel[] allergies = mapper.readValue(json, IAllergyModel[].class);
                
                int cnt = 0;
                for (IAllergyModel am : allergies) {
                    ObservationModel om = am.toObservationModel();
                    ehtService.addAllergy(om);
                    cnt++;
                }
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));

            }
        };
    }
    
    @PUT
    @Path("/allergy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput putAllergies(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                IAllergyModel[] allergies = mapper.readValue(json, IAllergyModel[].class);
                
                int cnt = 0;
                for (IAllergyModel am : allergies) {
                    ObservationModel om = am.toObservationModel();
                    ehtService.updateAllergy(om);
                    cnt++;
                }

                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    
    @DELETE
    @Path("/allergy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput deleteAllergies(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                IAllergyModel[] allergies = mapper.readValue(json, IAllergyModel[].class);

                int cnt = 0;
                for (IAllergyModel am : allergies) {
                    ObservationModel om = am.toObservationModel();
                    ehtService.deleteAllergy(om);
                    cnt++;
                }
                
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    
    @GET
    @Path("/diagnosis/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getDiagnosis(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                String [] params = param.split(",");
                
                long ptPK = Long.parseLong(params[0]);
                boolean active = (params.length>1) ?  Boolean.parseBoolean(params[1]) : true;
                int first = (params.length>2) ? Integer.parseInt(params[2]) : 0;
                int maxResult = (params.length>3) ? Integer.parseInt(params[3]) : 100;
                boolean outcomeOnly = (params.length>4) ?  Boolean.parseBoolean(params[4]) : false;
                
                List<RegisteredDiagnosisModel> list = ehtService.getDiagnosis(ptPK, active, outcomeOnly, first, maxResult);
                List<IRegisteredDiagnosis> result = new ArrayList(list.size());
                for (RegisteredDiagnosisModel model : list) {
                    IRegisteredDiagnosis ir = new IRegisteredDiagnosis();
                    ir.fromModel(model);
                    result.add(ir);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @POST
    @Path("/diagnosis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput postDicease(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                IRegisteredDiagnosis[] list = mapper.readValue(json, IRegisteredDiagnosis[].class);
                
                int cnt = 0;
                for (IRegisteredDiagnosis ir : list) {
                    RegisteredDiagnosisModel model = ir.toModel();
                    ehtService.addDiagnosis(model);
                    cnt++;
                }
                
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));

            }
        };
    }
    
    @PUT
    @Path("/diagnosis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput putDicease(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                IRegisteredDiagnosis[] list = mapper.readValue(json, IRegisteredDiagnosis[].class);
                
                int cnt = 0;
                for (IRegisteredDiagnosis ir : list) {
                    RegisteredDiagnosisModel model = ir.toModel();
                    ehtService.updateDiagnosis(model);
                    cnt++;
                }
                      
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
                
            }
        };
    }
    
    @DELETE
    @Path("/diagnosis")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput deleteDicease(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                IRegisteredDiagnosis[] list = mapper.readValue(json, IRegisteredDiagnosis[].class);
                                
                int cnt = 0;
                for (IRegisteredDiagnosis ir : list) {
                    RegisteredDiagnosisModel model = ir.toModel();
                    ehtService.deleteDiagnosis(model);
                    cnt++;
                }
                
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    
    @GET
    @Path("/karteNumber/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getEHTKarte(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long ptPK = Long.parseLong(param);
                KarteNumber karte = ehtService.getKarteNumber(ptPK);
                karte.setNumber(getFacilityCodeBy1001());
                IKarteNumber ieht = new IKarteNumber();
                ieht.setModel(karte);
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, ieht);
            }
        };
    }
    
    @GET
    @Path("/docinfo/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getFastDocInfoList(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long ptPK = Long.parseLong(param);
                List<DocInfoModel> list = ehtService.getDocInfoList(ptPK);
                final List<IFastDocInfo> result = new ArrayList(list.size());
                for (DocInfoModel m : list) {
                    IFastDocInfo idoc = new IFastDocInfo();
                    idoc.fromModel(m);
                    result.add(idoc);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    
    @GET
    @Path("/document/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getDocument(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long docPK = Long.parseLong(param);
                DocumentModel doc = ehtService.getDocumentByPk(docPK);
                doc.toDetuch();
//                if (doc.getUserModel()!=null) {
//                    System.err.println("doc.getUserModel()!=null");
//                    System.err.println(doc.getUserModel().getCommonName());
//                }
//                else {
//                    System.err.println("doc.getUserModel()==null");
//                }
                IDocument idoc = new IDocument();
                idoc.fromModel(doc);
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, idoc);
            }
        };
    }
    
    // S.Oh 2014/02/06 iPadのFreeText対応 Add Start
    @GET
    @Path("/document2/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getDocument2(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long docPK = Long.parseLong(param);
                DocumentModel doc = ehtService.getDocumentByPk(docPK);
                doc.toDetuch();
                IDocument2 idoc = new IDocument2();
                idoc.fromModel(doc);
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, idoc);
            }
        };
    }
    // S.Oh 2014/02/06 Add End
    
//s.oh^ 2014/08/20 添付ファイルの別読
    @GET
    @Path("/attachment/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getAttachment(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long id = Long.parseLong(param);
                AttachmentModel result = ehtService.getAttachment(id);
                IAttachmentModel iam = new IAttachmentModel();
                iam.fromModel(result);
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, iam);
            }
        };
    }
//s.oh$
    
    @PUT
    @Path("/sendClaim")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput sendPackage(final String json) throws IOException {
        
        return new StreamingOutput() {
            
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                ObjectMapper mapper = new ObjectMapper();
                ISendPackage pkg = mapper.readValue(json, ISendPackage.class);

                // カルテ文書
                DocumentModel model = pkg.documentModel();
                if (model!=null) {
                    karteService.sendDocument(model);
                }

                // Status更新
                ChartEventModel cvt = pkg.chartEventModel();
                if (cvt!=null) {
                    eventServiceBean.processChartEvent(cvt);
                } else {
                }
                os.write(0);
            }
        };
    }
    
    // S.Oh 2014/02/06 iPadのFreeText対応 Add Start
    @PUT
    @Path("/sendClaim2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput sendPackage2(final String json) throws IOException {
        
        return new StreamingOutput() {
            
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                ObjectMapper mapper = new ObjectMapper();
                ISendPackage2 pkg = mapper.readValue(json, ISendPackage2.class);

                // カルテ文書
                DocumentModel model = pkg.documentModel();
                if (model!=null) {
                    karteService.sendDocument(model);
                }

                // Status更新
                ChartEventModel cvt = pkg.chartEventModel();
                if (cvt!=null) {
                    eventServiceBean.processChartEvent(cvt);
                } else {
                }
                os.write(0);
            }
        };
    }
    // S.Oh 2014/02/06 Add End
    
    @DELETE
    @Path("/document")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput deleteDocument(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String[] pks = mapper.readValue(json, String[].class);
                
                long pk = Long.parseLong(pks[0]);
                List<String> list = ehtService.deleteDocumentByPk(pk);
                
                mapper = getSerializeMapper();
                mapper.writeValue(os, list);
            }
        };
    }
    
    @GET
    @Path("/order/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput collectModules(final @PathParam("param") String param) {
        
        return new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                
                String [] params = param.split(",");
                
                long pk = Long.parseLong(params[0]);            // patientPK
                
                Date fromDate = IOSHelper.toDate(params[1]);    // fromDate
                Date toDate = IOSHelper.toDate(params[2]);      // tOdate
                
                List<String> entities = null;
                // 併用禁忌不具合対応
                //if (params.length>3)
                //{
                //    entities = new ArrayList(2);
                //    for (int i=3; i <params.length; i++) {
                //        entities.add(params[i]);                     // entity
                //    }
                //}
                //
                //List<ModuleModel> list = ehtService.collectModules(pk, fromDate, toDate, entities);
                //List<IBundleModule> result = new ArrayList(list.size());
                //
                //for (ModuleModel module : list) {
                //
                //    if (module.getModel() instanceof BundleDolphin) {
                //
                //        IBundleModule ib = new IBundleModule();
                //        ib.fromModel(module);
                //        // trick
                //        //if (module.getModel() instanceof BundleDolphin) {
                //            BundleDolphin bd = (BundleDolphin)module.getModel();
                //            ib.getModel().setOrderName(bd.getOrderName());
                //        //} else {
                //            //ib.getModel().setOrderName(module.getModuleInfoBean().getEntity());
                //        //}
                //        result.add(ib);
                //    }
                //}
                if (params.length>3)
                {
                    entities = new ArrayList(2);
                    for (int i=3; i <params.length; i++) {
                        entities.add(params[i]);                     // entity
                    }
                }else{
                    entities = new ArrayList(2);
                    entities.add(IInfoModel.ENTITY_MED_ORDER);
                }
                
                List<ModuleModel> list = ehtService.collectModules(pk, fromDate, toDate, entities);
                List<IBundleModule> result = new ArrayList(list.size());

                for (ModuleModel module : list) {
                    if (module.getModel() instanceof BundleDolphin) {
                        IBundleModule ib = new IBundleModule();
                        ib.fromModel(module);
                        BundleDolphin bd = (BundleDolphin)module.getModel();
                        ib.getModel().setOrderName(bd.getOrderName());
                        result.add(ib);
                    }else{
                        IBundleModule ib = new IBundleModule();
                        ib.fromModel(module);
                        result.add(ib);
                    }
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(output, result);
            }
        };
    }
    
    @GET
    @Path("/module/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getModule(final @PathParam("param") String param) {

        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                String [] params = param.split(",");

                long pk = Long.parseLong(params[0]);
                String entity = params[1];
                int firstResult = Integer.parseInt(params[2]);
                int maxResult = Integer.parseInt(params[3]);

                List<ModuleModel> list = ehtService.getModules(pk, entity, firstResult, maxResult);
                List<IBundleModule> result = new ArrayList(list.size());

                for (ModuleModel module : list) {

                    IBundleModule ib = new IBundleModule();
                    ib.fromModel(module);
                    // trick
                    if (module.getModel() instanceof BundleDolphin) {
                        BundleDolphin bd = (BundleDolphin)module.getModel();
                        ib.getModel().setOrderName(bd.getOrderName());
                    } else {
                        ib.getModel().setOrderName(module.getModuleInfoBean().getEntity());
                    }
                    result.add(ib);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };    
    }
    
    @GET
    @Path("/module/last/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getLastModule(final @PathParam("param") String param) {

        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                String [] params = param.split(",");

                long pk = Long.parseLong(params[0]);
                String entity = params[1];
//minagawa^ ToDo 指定できるようにする               
                //int firstResult = Integer.parseInt(params[2]);
                //int maxResult = Integer.parseInt(params[3]);
//minagawa$
                List<ModuleModel> list = ehtService.getLastModule(pk, entity);
                List<IBundleModule> result = new ArrayList(list.size());

                for (ModuleModel module : list) {

                    IBundleModule ib = new IBundleModule();
                    ib.fromModel(module);
                    // trick
                    if (module.getModel() instanceof BundleDolphin) {
                        BundleDolphin bd = (BundleDolphin)module.getModel();
                        ib.getModel().setOrderName(bd.getOrderName());
                    } else {
                        ib.getModel().setOrderName(module.getModuleInfoBean().getEntity());
                    }
                    result.add(ib);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };    
    }
    
    @GET
    @Path("/module/laboTest/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getLaboTest(final @PathParam("param") String param) {
 
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                String [] params = param.split(",");
                String facilityId = params[0];
                String patientId = params[1];
                int firstResult = Integer.parseInt(params[2]);
                int maxResult = Integer.parseInt(params[3]);

                List<NLaboModule> list = ehtService.getLaboTest(facilityId, patientId, firstResult, maxResult);
                List<NLaboModuleConverter> result = new ArrayList(list.size());
                for (NLaboModule module : list) {
                    NLaboModuleConverter conv = new NLaboModuleConverter();
                    conv.setModel(module);
                    result.add(conv);
                }
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        }; 
    }
    
    @GET
    @Path("/item/laboItem/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getLaboGraph(@PathParam("param") String param) {

        String [] params = param.split(",");
        
        String facilityId = params[0];
        String patientId = params[1];
        int firstResult = Integer.parseInt(params[2]);
        int maxResult = Integer.parseInt(params[3]);
        String itemCode = params[4];

        List<NLaboItem> list  = ehtService.getLaboTestItem(facilityId, patientId, firstResult, maxResult, itemCode);
        int cnt = list.size();
        
        final ILaboGraphItem graph = new ILaboGraphItem();

        if (cnt==0) {
            return new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    ObjectMapper mapper = getSerializeMapper();
                    mapper.writeValue(os, graph);
                }
            }; 
        }

        // この検査項目の共通情報を出力する
        NLaboItem item = list.get(cnt-1);
        
        // 検査項目コード
        graph.setItemCode(item.getItemCode());

        // 検査項目名
        graph.setItemName(item.getItemName());

        // 基準値
        graph.setNormalValue(item.getNormalValue());

        // 単位
        graph.setUnit(item.getUnit());

        // sampleDate の逆順で結果データを出力する
        for (int k = 0; k < cnt; k++) {

            item = list.get(k);
            ILaboValue value = new ILaboValue();

            // sampleDate
            value.setSampleDate(item.getSampleDate());

            // value
            value.setValue(item.getValue());

            // comment1
            value.setComment1(item.getComment1());

            // comment2
            value.setComment2(item.getComment2());
            
            graph.addValue(value);
        }

        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, graph);
            }
        }; 
    }
    
    @GET
    @Path("/stampTree/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getStampTree(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                long pk = Long.parseLong(param);
                String json = getTreeJson(pk);
                os.write(json.getBytes());
            }
        };
    }
    
    private String getTreeJson(long userPK) {
        
        IStampTreeModel treeModel = ehtService.getTrees(userPK);
        
        try {
            String treeXml = new String(treeModel.getTreeBytes(), "UTF-8");
            BufferedReader reader = new BufferedReader(new StringReader(treeXml));
            JSONStampTreeBuilder builder = new JSONStampTreeBuilder();
            StampTreeDirector director = new StampTreeDirector(builder);
            String json = director.build(reader);
            reader.close();
            return json;
        } catch (UnsupportedEncodingException ex) {
            //System.err.println("getTreeJson:" + ex.getMessage());
        } catch (IOException ex) {
            //System.err.println("getTreeJson:" + ex.getMessage());
        }
        return null;
    }
    
    @GET
    @Path("/stamp/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getStamp(final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {

                StampModel stampModel = ehtService.getStamp(param);
                if (stampModel!=null) {
                    XMLDecoder d = new XMLDecoder(
                        new BufferedInputStream(
                        new ByteArrayInputStream(stampModel.getStampBytes())));
                    InfoModel model = (InfoModel)d.readObject();
                    JSONStampBuilder builder = new JSONStampBuilder();
                    String json = builder.build(model);
                    os.write(json.getBytes());
                } else {
                    os.write(null);
                }
            }
        };
    }
    
    /**
     * 保健医療機関コードとJMARIコードを取得する。
     * @return 
     */
    private String getFacilityCodeBy1001() {
       
//s.oh^ 2013/10/17 ローカルORCA対応
        try {
            // custom.properties から 保健医療機関コードとJMARIコードを読む
            Properties config = new Properties();
            // コンフィグファイルを読み込む
            StringBuilder sb = new StringBuilder();
            sb.append(System.getProperty("jboss.home.dir"));
            sb.append(File.separator);
            sb.append("custom.properties");
            File f = new File(sb.toString());
            FileInputStream fin = new FileInputStream(f);
            InputStreamReader r = new InputStreamReader(fin, "JISAutoDetect");
            config.load(r);
            r.close();
            // JMARI code
            String jmari = config.getProperty("jamri.code");
            String hcfacility = config.getProperty("healthcarefacility.code");
            if(jmari != null && jmari.length() == 12 && hcfacility != null && hcfacility.length() == 10) {
                StringBuilder ret = new StringBuilder();
                ret.append(hcfacility);
                ret.append("JPN");
                ret.append(jmari);
                return ret.toString();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(EHTResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(EHTResource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EHTResource.class.getName()).log(Level.SEVERE, null, ex);
        }
//s.oh$
        // SQL 文
        StringBuilder buf = new StringBuilder();
        buf.append(QUERY_FACILITYID_BY_1001);
        String sql = buf.toString();

        Connection con = null;
        PreparedStatement ps;
        
        StringBuilder ret = new StringBuilder();

        try {
            //con = ds.getConnection();
            con = getConnection();
            ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String line = rs.getString(1);
                
                // 保険医療機関コード 10桁
                ret.append(line.substring(0, 10));
                
                // JMARIコード JPN+12桁 (total 15)
                int index = line.indexOf("JPN");
                if (index>0) {
                    ret.append(line.substring(index, index+15));
                }
            }
            rs.close();
            ps.close();
            con.close();
            con = null;

        } catch (Exception e) {
            e.printStackTrace(System.err);

        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                }
            }
        }

        return ret.toString();        
    }
    
    @PUT
    @Path("/interaction")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput checkInteraction(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {

                ObjectMapper mapper = new ObjectMapper();
                InteractionCodeList input = mapper.readValue(json, InteractionCodeList.class);
                
//                if (input.getCodes1()!=null)
//                {
//                    for (String code : input.getCodes1()) {
//                        System.err.println(code);
//                    }
//                }
//                if (input.getCodes2()!=null)
//                {
//                    for (String code : input.getCodes2()) {
//                        System.err.println(code);
//                    }
//                }

                // 相互作用モデルのリスト
                List<DrugInteractionModel> ret = new ArrayList<DrugInteractionModel>();

                if (input.getCodes1() == null       || 
                        input.getCodes1().isEmpty() || 
                        input.getCodes2() == null   || 
                        input.getCodes2().isEmpty()) {
                    mapper = getSerializeMapper();
                    mapper.writeValue(os, ret);
                }

                // SQL文を作成
                StringBuilder sb = new StringBuilder();
                sb.append("select drugcd, drugcd2, TI.syojyoucd, syojyou ");
                sb.append("from tbl_interact TI inner join tbl_sskijyo TS on TI.syojyoucd = TS.syojyoucd ");
                sb.append("where (drugcd in (");
                sb.append(getCodes(input.getCodes1()));
                sb.append(") and drugcd2 in (");
                sb.append(getCodes(input.getCodes2()));
                sb.append("))");
                String sql = sb.toString();

                Connection con = null;
                Statement st = null;

                try {
                    con = getConnection();
                    st = con.createStatement();
                    ResultSet rs = st.executeQuery(sql);

                    while (rs.next()) {
                        ret.add(new DrugInteractionModel(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                    }
                    rs.close();
                    closeStatement(st);
                    closeConnection(con);
                    mapper = getSerializeMapper();
                    mapper.writeValue(os, ret);
                    
                } catch (Exception e) {
                    processError(e);
                    closeStatement(st);
                    closeConnection(con);
                    throw new WebApplicationException(e);
                }
            }
        };
    }
    
    // バイタル対応
    @GET
    @Path("/vital/pat/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getPatVital(final @Context HttpServletRequest servletReq, final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                String fpid = getFidPid(servletReq.getRemoteUser(), param);
                List<VitalModel> list = ehtService.getPatVital(fpid);
                List<IVitalModel> result = new ArrayList();
                for (VitalModel m : list) {
                    IVitalModel ac = new IVitalModel();
                    ac.fromModel(m);
                    result.add(ac);
                }
                mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    @POST
    @Path("/vital")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput postVital(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                IVitalModel imodel = mapper.readValue(json, IVitalModel.class);
                VitalModel model = imodel.toModel();
                int cnt = ehtService.addVital(model);
                          
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    @DELETE
    @Path("/vital/id/{param}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput removeVital(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                int cnt = ehtService.removeVital(json);
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    @GET
    @Path("/physical/karteid/{param}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getKartePhysical(final @Context HttpServletRequest servletReq, final @PathParam("param") String param) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                long karteId = Long.parseLong(param);
                List<IPhysicalModel> result = ehtService.getPhysicals(karteId);
                mapper = getSerializeMapper();
                mapper.writeValue(os, result);
            }
        };
    }
    @POST
    @Path("/physical")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput postPhysical(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                IPhysicalModel physical = mapper.readValue(json, IPhysicalModel.class);
                int cnt = 0;
                List<ObservationModel> om = physical.toObservationModel();
                ehtService.addObservations(om);
                
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(1));
            }
        };
    }
    @DELETE
    @Path("/physical/id/{param}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput removePhysical(final String json) {
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                
                String[] params = json.split(CAMMA);
                List<Long> list = new ArrayList<Long>(params.length);
                for (String s : params) {
                    list.add(Long.parseLong(s));
                }
                
                int cnt = ehtService.removeObservations(list);
                mapper = getSerializeMapper();
                mapper.writeValue(os, String.valueOf(cnt));
            }
        };
    }
    
    // サーバー情報の取得
    @GET
    @Path("/claim/conn")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getClaimConn() {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = getSerializeMapper();
                mapper.writeValue(os, getProperty(CLAIM_CONN));
            }
        };
    }
    
    @GET
    @Path("/serverinfo")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getServerInfo() {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
                ObjectMapper mapper = getSerializeMapper();
                
                StringBuilder sb = new StringBuilder();
                sb.append((getProperty(MOBILE_KIND).toLowerCase().equals("cis")) ? "1" : "0");
                sb.append(",");
                sb.append((getProperty(MOBILE_ONOFF).toLowerCase().equals("on")) ? "1" : "0");
                
                mapper.writeValue(os, sb.toString());
//                mapper.writeValue(os, getProperty(MOBILE_ONOFF));
//                mapper.writeValue(os, getProperty(SERVER_VERSION));
//                mapper.writeValue(os, getProperty(DOLPHIN_FACILITYID));
//                mapper.writeValue(os, getProperty(JAMRI_CODE));
//                mapper.writeValue(os, getProperty(USE_AS_PVTSERVER));
//                mapper.writeValue(os, getProperty(PVT_LISTEN_BINDIP));
//                mapper.writeValue(os, getProperty(PVT_LISTEN_PORT));
//                mapper.writeValue(os, getProperty(PVT_LISTEN_ENCODING));
//                mapper.writeValue(os, getProperty(CLAIM_CONN));
//                mapper.writeValue(os, getProperty(CLAIM_HOST));
//                mapper.writeValue(os, getProperty(CLAIM_SEND_PORT));
//                mapper.writeValue(os, getProperty(CLAIM_SEND_ENCODING));
//                mapper.writeValue(os, getProperty(CLAIM_JDBC_URL));
//                mapper.writeValue(os, getProperty(CLAIM_USER));
//                mapper.writeValue(os, getProperty(CLAIM_PASSWORD));
//                mapper.writeValue(os, getProperty(RP_DEFAULT_INOUT));
//                mapper.writeValue(os, getProperty(PVTLIST_CLEAR));
            }
        };
    }
    
    public String getProperty(String item) {
        Properties config = new Properties();
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("jboss.home.dir"));
        sb.append(File.separator);
        sb.append("custom.properties");
        File f = new File(sb.toString());
        try {
            FileInputStream fin = new FileInputStream(f);
            //InputStreamReader isr = new InputStreamReader(fin, "JISAutoDetect");
            InputStreamReader isr = new InputStreamReader(fin, "UTF-8");
            config.load(isr);
            isr.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return config.getProperty(item, "");
    }
     
    // srycdのListからカンマ区切りの文字列を作る
    private String getCodes(Collection<String> srycdList){

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String srycd : srycdList){
            if (!first){
                sb.append(",");
            } else {
                first = false;
            }
            sb.append(addSingleQuote(srycd));
        }
        return sb.toString();
    }
    
    private String addSingleQuote(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("'").append(str).append("'");
        return sb.toString();
    }
    
    private Connection getConnection() {
        return ORCAConnection.getInstance().getConnection();
    }
    
    private void closeStatement(java.sql.Statement st) {
        if (st != null) {
            try {
                st.close();
            }
            catch (SQLException e) {
            	e.printStackTrace(System.err);
            }
        }
    }
    
    private void closeConnection(Connection c) {
        try {
            c.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    private void processError(Throwable e) {
        e.printStackTrace(System.err);
    }
    
}
