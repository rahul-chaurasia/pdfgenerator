package com.doubtnut.assignment.controller;

import com.doubtnut.assignment.pdfgenerator.DataObject;
import com.doubtnut.assignment.pdfgenerator.HeaderFooter;
import com.doubtnut.assignment.pdfgenerator.PDFCreator;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*
@description : PDF generator for questions which is search by a user on the web/app but in below mentioned class few assumptions has taken care of those are :
1- This controller(end point) just taking care of PDF generation with its data
2- Here PDF data is set by method (getDataObjectList) manually for single user only, In real world we can get all users which are inactive in last 5 mins from db (mysql | Redis) where questions data is stored
3- Generated files are permanently saved on the  server in PDF_Reports folder and also downloadable in browser too
5- There are 3 options to manage 4-5 mins delay time
a) We can identify user inactivity on basis of user session and heartbeat api call send to the the system
c) We can write a script(cron) which get all users which are inactive since last 5 mins and generate PDF for all users  and send  that to queue for sending email and sms whatever is required .I used this #2 approach and written code for single user only
*/


@RestController
@RequestMapping(value = "api")
public class PdfGeneratorController {

    private static final String DIR = "/home/RahulChaurasiya/Downloads/doubtnuttest-master/Question_Pdf";
    private static final String TITLE = "DoubtNut_QuestionReport_";
    public static final String PDF_EXTENSION = ".pdf";

    @Autowired(required = false)
    private SessionRegistry sessionRegistry;

    @RequestMapping(value = "/pdf/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_PDF_VALUE)
    public void downloadPDF(HttpServletRequest request, HttpServletResponse response)
            throws IOException{
        try {
            //Step 1-get users and user's questions who are inactive in last 5 mins and generate PDF for them
            // Assume there are multiple users but i am writing logic for a single user only and pass userId as static one
            List<User> inactiveUsers = getInactiveUsers(); // get data from redis here just created array list
            if(inactiveUsers == null){
                inactiveUsers = new ArrayList<>();
            }
            Iterator itr=inactiveUsers.iterator();
            Long userId ;
            while(itr.hasNext() || inactiveUsers.isEmpty()) {
                if(!inactiveUsers.isEmpty()) {
                     userId = (Long) itr.next();
                }else{
                    userId = 1L;
                }
                String pdfName = DIR + "/" + TITLE + userId + "_" + System.currentTimeMillis() + PDF_EXTENSION;
                pdfGenerator(pdfName, userId); // Generate the PDF in the Question_Pdf(On server)
                // Now download the file in browser starts here
                response.setContentType("application/pdf");
                response.setHeader("Content-disposition", "attachment;filename=" + TITLE + userId + "_" + System.currentTimeMillis() + PDF_EXTENSION);
                File f = new File(pdfName);
                FileInputStream fis = new FileInputStream(f);
                DataOutputStream os = new DataOutputStream(response.getOutputStream());
                response.setHeader("Content-Length", String.valueOf(f.length()));
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = fis.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
                // below functionality is used for mailing or queuing purposes.
                // send data to Queue to send this PDF in email or SMS starts here
                sendDatatoQueue(pdfName, userId, buffer);
                // Send data to Queue to send this PDF in email or SMS ends here
                if(inactiveUsers.isEmpty()){
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
   @author :Rahul Chaurasia
   @description : get Inactive Users only
   */

    private List<User> getInactiveUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<SessionInformation> sessionInformations = listInactiveUsersLoggedInUsers();
            if(sessionInformations != null) {
                sessionInformations.stream().forEach(sessionInformation -> {
                    users.add((User) sessionInformation.getPrincipal());
                });
            }
        return users;
        } catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }

    /**
     * this method will fetch all inactive users from session.
     * @return
     */
    public List<SessionInformation> listInactiveUsersLoggedInUsers() {
        try {
            final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

            for (final Object principal : allPrincipals) {
                if (principal instanceof SecurityProperties.User) {
                    final SecurityProperties.User user = (SecurityProperties.User) principal;

                    // fetching Active suers
                    List<SessionInformation> activeUserSessions =
                            sessionRegistry.getAllSessions(principal,
                                    /* includeExpiredSessions */ false); // Should not return null;

                    // fetching InActive suers
                    List<SessionInformation> inActiveUserSessions =
                            sessionRegistry.getAllSessions(principal,
                                    /* includeExpiredSessions */ true);

                    if (!inActiveUserSessions.isEmpty()) {
                        // Do something with user
                        System.out.println(user);
                        return inActiveUserSessions;
                    }
                }
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    /*
    @author :Rahul Chaurasia
    @description : This function used for send data
    @params : pdfName(String)
    @return :No return Void type
    */
    private void sendDatatoQueue(String pdfName, Long userId, byte[] data) {
        try{
            // configure  queue and send data to it.
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
     @author : Rahul Chaurasia
    @description : This function  accept data object and generate PDF
    */
    private void pdfGenerator(String pdfName,Long userId) {
        List<DataObject> dataObjList = getSimilarQuestions(userId);
        Document document = null;
        try {
            //Document is not auto-closable hence need to close it separately
            document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(
                    new File(pdfName)));
            HeaderFooter event = new HeaderFooter();
            event.setHeader("DoubtNut Question Report");
            writer.setPageEvent(event);
            document.open();
            String finaltitle = TITLE+userId;
            PDFCreator.addMetaData(document, finaltitle); // set meta Title
            PDFCreator.addTitlePage(document, finaltitle); // set Page Title
            PDFCreator.addContent(  document, dataObjList); // set PDF content here
        }catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("FileNotFoundException occurs.." + e.getMessage());
        }finally{
            if(null != document){
                document.close();
            }
        }
    }


    /*
    @author :Rahul Chaurasia
    @description : get User data from redis list but here I have set data manually to test the report
    @return : datalist object(JSON)
    */
    private static List<DataObject> getSimilarQuestions(Long userId ){
        // get this data from redis here in case mysql table too
        // here just passed to test the PDF functionality
        List<DataObject> dataObjList = new ArrayList<DataObject>();
        DataObject d1 = new DataObject();
        d1.setQuestion("What is Blocking Queue ?");
        dataObjList.add(d1);
        DataObject d2 = new DataObject();
        d2.setQuestion("What is Inheritance ?");
        dataObjList.add(d2);
        DataObject d3 = new DataObject();
        d3.setQuestion("Define Oops Concept?");
        dataObjList.add(d3);
        DataObject d4 = new DataObject();
        d4.setQuestion("What is itext Pdf genration library ?");
        dataObjList.add(d4);
        return dataObjList;
    }
}
