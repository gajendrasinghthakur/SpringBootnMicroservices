package com.csc.fsg.nba.accel.process;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL3874</td><td>Discretionary</td><td></td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 
 * @since New Business Accelerator - Version 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.statusDefinitions.Status;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

public class AxaStatusDefinitionHelper {
    private static Map qualifierMap = new HashMap();
    
    static {
        qualifierMap.put("${MESSAGE}", new MessageQualifier());
        qualifierMap.put("${UNDQ}", new UndqQualifier());
        qualifierMap.put("${CSMQ}", new CsmqQualifier());
        qualifierMap.put("${AWDSTATUS}", new AwdStatusQualifier()); //APSL3874 Fixed Code
        qualifierMap.put("${AWDWORKTYPE}", new AwdWorkTypeQualifier());
        qualifierMap.put("${AWDQUEUE}", new AwdQueueQualifier());
        qualifierMap.put("${USERID}", new UserIdQualifier());
        qualifierMap.put("${RQUI}", new ReqIdQualifier());
        qualifierMap.put("${DIST}", new DistQualifier()); // APSL4149
        qualifierMap.put("${AWDQUEUETRANS}", new AwdQueueTransQualifier());//APSL4149
        qualifierMap.put("${AWDSTATUSTRANS}", new AwdStatusTransQualifier());//APSL4149
        qualifierMap.put("${UWSTATPREF}", new UwStatusQualifier()); // APSL5128
    }
    
    public static String determineComment(Status aStatus, NbaAutomatedProcess autoProcess) {
        return determineComment(aStatus, autoProcess, null);
    }
    
    public static String determineComment(Status aStatus, NbaAutomatedProcess autoProcess, Throwable exp) {
        return resolveStatusDefinitionQualifiers(aStatus.getComment(), autoProcess, exp);
    }
    
    public static String determineStatus(Status aStatus, NbaAutomatedProcess autoProcess) {
        return resolveStatusDefinitionQualifiers(aStatus.getStatusCode(), autoProcess);
    }
    
	public static String determineStatus(Status aStatus, NbaDst nbaDst, NbaTXLife nbaTXLife) {
		return resolveStatusDefinitionQualifiers(aStatus.getStatusCode(), nbaDst, nbaTXLife);
	}
    public static String determineRoutingReason(Status aStatus, NbaAutomatedProcess autoProcess) {
        return resolveStatusDefinitionQualifiers(aStatus.getRoutingReason(), autoProcess);
    }
    
    protected static String resolveStatusDefinitionQualifiers(String inputString, NbaAutomatedProcess autoProcess) {
        return resolveStatusDefinitionQualifiers(inputString, autoProcess, null);
    }
    
    //APSL5128 New Method
	protected static String resolveStatusDefinitionQualifiers(String inputString, NbaDst nbaDst, NbaTXLife nbaTXLife) {
		int index = inputString.indexOf("${");
		if (index > -1) {
			List parameters = new ArrayList();
			Pattern aPattern = Pattern.compile("(\\$)(\\{)(\\w+)(})");
			Matcher aMatcher = aPattern.matcher(inputString);
			while (aMatcher.find()) {
				parameters.add(aMatcher.group());
			}
			int size = parameters.size();
			for (int i = 0; i < size; i++) {
				inputString = inputString.replaceFirst("\\Q" + (String) parameters.get(i) + "\\E",
						resolveQualifier((String) parameters.get(i), nbaDst, nbaTXLife));
			}
		}
		return inputString;
	}
    protected static String resolveStatusDefinitionQualifiers(String inputString, NbaAutomatedProcess autoProcess, Throwable exp) {
        int index = inputString.indexOf("${");
        if (index > -1) {
            List parameters = new ArrayList();
            Pattern aPattern = Pattern.compile("(\\$)(\\{)(\\w+)(})");
            Matcher aMatcher = aPattern.matcher(inputString);
            while (aMatcher.find()) {
                parameters.add(aMatcher.group());
            }
            int size = parameters.size();
            for (int i = 0; i < size; i++) {
                inputString = inputString.replaceFirst("\\Q" + (String) parameters.get(i) + "\\E", resolveQualifier((String) parameters.get(i),
                        autoProcess, exp));
            }
        }
        return inputString;
    }
    
    protected static String resolveQualifier(String qualifier, NbaAutomatedProcess autoProcess, Throwable exp) {
        QualifierResolver resolver = (QualifierResolver) qualifierMap.get(qualifier);
        resolver.setAutoProcess(autoProcess);
        resolver.setException(exp);
        return resolver.resolve();
    }
    // APSL5128 New Method
	protected static String resolveQualifier(String qualifier, NbaDst nbaDst, NbaTXLife nbaTxlife) {
		QualifierResolver resolver = (QualifierResolver) qualifierMap.get(qualifier);
		resolver.setNbaDst(nbaDst);
		resolver.setNbaTxlife(nbaTxlife);
		return resolver.resolve();
	}
}

interface QualifierResolver {
    public String resolve();
    public void setException(Throwable exception);
    public void setAutoProcess(NbaAutomatedProcess autoProcess);
    public void setNbaDst(NbaDst nbaDst); //APSL5128
    public void setNbaTxlife(NbaTXLife nbaTxlife); //APSL5128
}

abstract class QualifierResolverImpl implements QualifierResolver {
    private NbaAutomatedProcess autoProcess;
    private Throwable exception;
    private NbaDst nbaDst; //APSL5128
    private NbaTXLife nbaTxlife; //APSL5128
    
    public Throwable getException() {
        return exception;
    }
    public void setException(Throwable exception) {
        this.exception = exception;
    }
    public NbaAutomatedProcess getAutoProcess() {
        return autoProcess;
    }
    public void setAutoProcess(NbaAutomatedProcess autoProcess) {
        this.autoProcess = autoProcess;
    }
	public NbaDst getNbaDst() {
		return nbaDst;
	}
	public void setNbaDst(NbaDst nbaDst) {
		this.nbaDst = nbaDst;
	}
	public NbaTXLife getNbaTxlife() {
		return nbaTxlife;
	}
	public void setNbaTxlife(NbaTXLife nbaTxlife) {
		this.nbaTxlife = nbaTxlife;
	}

}

class UndqQualifier extends QualifierResolverImpl {
    public String resolve() {
        String value = "";
		if (getNbaDst().getNbaLob() != null) { // APSL5128
            Pattern bPattern = Pattern.compile("(UW)(\\d+)");
			Matcher bMatcher = bPattern.matcher((getNbaDst().getNbaLob().getUndwrtQueue())); // APSL5128
            if (bMatcher.find()) {
                value = bMatcher.group().substring(2);
            }
        }
        return value;
    }
}

class CsmqQualifier extends QualifierResolverImpl {
    public String resolve() {
        String value = "";
        if (getAutoProcess().getNbaTxLife() != null) {
            Pattern bPattern = Pattern.compile("(UCM)([RW])(\\d+)");
            Matcher bMatcher = bPattern.matcher(getAutoProcess().getNbaTxLife().getPolicy().getApplicationInfo().getNBContactName());
            if (bMatcher.find()) {
                value = bMatcher.group().substring(3);
            }
        }
        return value;
    }
}

class MessageQualifier extends QualifierResolverImpl {
	public String resolve() {
		return (getException() != null) ? ((getException().getMessage() != null) ? getException().getMessage() : "") : "";
	}
} 

class AwdStatusQualifier extends QualifierResolverImpl {
    public String resolve() {
        return getAutoProcess().getWork().getStatus();
    }
}

class AwdQueueQualifier extends QualifierResolverImpl {
    public String resolve() {
        return getAutoProcess().getWork().getQueue();
    }
}

class AwdWorkTypeQualifier extends QualifierResolverImpl {
    public String resolve() {
        return getAutoProcess().getWork().getWorkType();
    }
}

class UserIdQualifier extends QualifierResolverImpl {
    public String resolve() {
        return getAutoProcess().getUser().getUserID();
    }
}
class ReqIdQualifier extends QualifierResolverImpl {
    public String resolve() {
        return getAutoProcess().getWork().getNbaLob().getReqUniqueID();
    }
}

// APSL4149
class DistQualifier extends QualifierResolverImpl {
	public String resolve() {
		if (getAutoProcess().getNbaTxLife() != null) {
			PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getAutoProcess().getNbaTxLife().getPolicy());
			if (policyExtension != null && policyExtension.getDistributionChannel() == NbaOliConstants.OLI_DISTCHAN_10) {
				return "R";
			}
			return "W";
		} else if (getAutoProcess().getWork() != null && getAutoProcess().getWork().getNbaLob() != null) {
			try {
				if (getAutoProcess().getWork().getNbaLob().getDistChannel() == NbaOliConstants.OLI_DISTCHAN_10) {
					return "R";
				}
				return "W";
			} catch (NbaBaseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
//APSL4149
class AwdQueueTransQualifier extends QualifierResolverImpl {
    public String resolve() {
        NbaTableAccessor nta = new NbaTableAccessor();
        try {
            return nta.getAWDQueueTranslation(getAutoProcess().getWork().getBusinessArea(), getAutoProcess().getWork().getQueue());
        } catch (NbaDataAccessException e) {
            e.printStackTrace();
            return "";
        }
    }
}
//APSL4149
class AwdStatusTransQualifier extends QualifierResolverImpl {
    public String resolve() {
        NbaTableAccessor nta = new NbaTableAccessor();
        try {
            return nta.getStatusTranslationString(getAutoProcess().getWorkType(), getAutoProcess().getWork().getStatus());
        } catch (NbaBaseException e) {
            e.printStackTrace();
            return "";
        }
    }
}
//APSL5128 New Method
class UwStatusQualifier extends QualifierResolverImpl {
	public String resolve() {
		String underwriterQueue = getNbaDst().getNbaLob().getUndwrtQueue();
		if (!NbaUtils.isBlankOrNull(underwriterQueue) && underwriterQueue.length() > 6) {
			return "REQDTMD";
		}
		return "REQDTMND";
	}
}
