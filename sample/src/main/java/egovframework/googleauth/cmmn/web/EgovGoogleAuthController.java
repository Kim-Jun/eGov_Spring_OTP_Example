package egovframework.googleauth.cmmn.web;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class EgovGoogleAuthController extends HttpServlet {
	
	@RequestMapping(value = "/egovAuth.do") 
	public void main(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException{
		// Allocating the buffer
//      byte[] buffer = new byte[secretSize + numOfScratchCodes * scratchCodeSize];
        byte[] buffer = new byte[5 + 5 * 5];
         
        // Filling the buffer with random numbers.
        // Notice: you want to reuse the same random generator
        // while generating larger random number sequences.
        new Random().nextBytes(buffer);
 
        // Getting the key and converting it to Base32
        Base32 codec = new Base32();
//      byte[] secretKey = Arrays.copyOf(buffer, secretSize);
        byte[] secretKey = Arrays.copyOf(buffer, 5);
        byte[] bEncodedKey = codec.encode(secretKey);
         
        // 생성된 Key!
        String encodedKey = new String(bEncodedKey);
         
        System.out.println("encodedKey : " + encodedKey);
         
//      String url = getQRBarcodeURL(userName, hostName, secretKeyStr);
        // userName과 hostName은 변수로 받아서 넣어야 하지만, 여기선 테스트를 위해 하드코딩 해줬다.
        String url = getQRBarcodeURL("jh", "company.com", encodedKey); // 생성된 바코드 주소!
        System.out.println("URL : " + url);
         
        String view = "WEB-INF/jsp/egovframework/example/sample/googleAuth.jsp";
         
        req.setAttribute("encodedKey", encodedKey);
        req.setAttribute("url", url);
         
        req.getRequestDispatcher(view).forward(req, res);
		
	}

	private String getQRBarcodeURL(String user, String host, String secret) {
		// TODO Auto-generated method stub
		String format = "http://chart.apis.google.com/chart?cht=qr&amp;chs=300x300&amp;chl=otpauth://totp/%s@%s%%3Fsecret%%3D%s&amp;chld=H|0";
        
        return String.format(format, user, host, secret);
	}
	@RequestMapping(value = "/egovAuthPro.do")
	public String auth(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException{
		 String user_codeStr = req.getParameter("user_code");
	        long user_code = Integer.parseInt(user_codeStr);
	        String encodedKey = req.getParameter("encodedKey");
	        long l = new Date().getTime();
	        long ll =  l / 30000;
	         
	        boolean check_code = false;
	        try {
	            // 키, 코드, 시간으로 일회용 비밀번호가 맞는지 일치 여부 확인.
	            check_code = check_code(encodedKey, user_code, ll);
	        } catch (InvalidKeyException e) {
	            e.printStackTrace();
	        } catch (NoSuchAlgorithmException e) {
	            e.printStackTrace();
	        }
	         
	        // 일치한다면 true.
	        System.out.println("check_code : " + check_code);	
	        return "sample/googleAuthPro";
	}

	private boolean check_code(String secret, long code, long t) throws NoSuchAlgorithmException, InvalidKeyException {
		// TODO Auto-generated method stub
		Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
 
        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        int window = 3;
        for (int i = -window; i <= window; ++i) {
            long hash = verify_code(decodedKey, t + i);
 
            if (hash == code) {
                return true;
            }
        }
 
        // The validation code is invalid.
        return false;
	}

	private long verify_code(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException{
		// TODO Auto-generated method stub
		byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
 
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
 
        int offset = hash[20 - 1] & 0xF;
 
        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
 
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
 
        return (int) truncatedHash;
	}
}
