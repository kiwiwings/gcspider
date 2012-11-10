package de.kiwiwings.gccom.ListingParser.json;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class Log {
	@SerializedName("LogID") 
	public Long logId;
	@SerializedName("CacheID") 
	public Integer cacheId;
	@SerializedName("LogGuid") 
	public String logGuid;
	@SerializedName("LogType") 
	public String logType;
	@SerializedName("LogText") 
	public String logText;
	@SerializedName("Visited") 
	public Date visited;
	@SerializedName("UserName") 
	public String userName;
	@SerializedName("MembershipLevel") 
	public Integer membershipLevel;
	@SerializedName("AccountID") 
	public Integer accountId;
	@SerializedName("AccountGuid") 
	public String accountGuid;
	@SerializedName("AvatarImage") 
	public String avatarImage;
	@SerializedName("GeocacheFindCount") 
	public Integer geocacheFindCount;
	@SerializedName("GeocacheHideCount") 
	public Integer geocacheHideCount;
	@SerializedName("ChallengesCompleted") 
	public Integer challengesCompleted;
	@SerializedName("IsEncoded") 
	public Boolean isEncoded;
/*
	"LogID":189980980,
	"CacheID":2511073,
	"LogGuid":"534a2bec-f5fd-4e6b-826a-c69f85021d48",
	"Latitude":null,
	"Longitude":null,
	"LatLonString":"",
	"LogType":"Found it",
	"LogTypeImage":"icon_smile.gif",
	"LogText":"Guad, dass i gestern mid oder eha wega meim Suri aufm Kanapee blim bin, des mitm FTF war se ja nia ausganga, sovui Leid wia do scho auf da Roas warn. Aba heid in da Fria war i de erste! \t<img src=\"/images/icons/icon_smile_big.gif\" border=\"0\" align=\"middle\" ></img><br/><br/>Des R&#xE4;tsl war gestern glei gl&#xF6;st, i glab, dass des mitm D ja blos fia Preissn guid, fia Bayern is des a D oans! <br/><br/>Merce fia des Bixerl!<br/>Troublequeen (na, des &#xFC;bersetz i iatz ned a no ins Boarische!)"
	,"Created":"10/02/2011"
	,"Visited":"10/02/2011"
	,"UserName":"Troublequeen"
	,"MembershipLevel":3
	,"AccountID":2835288
	,"AccountGuid":"ee88440f-2833-4914-9679-7399ce938353"
	,"Email":""
	,"AvatarImage":"408181e7-51c7-4b41-852c-dd2ca76b99df.jpg"
	,"GeocacheFindCount":643
	,"GeocacheHideCount":3
	,"ChallengesCompleted":0
	,"IsEncoded":false
	,"creator":{"GroupTitle":"Premium Member"
	,"GroupImageUrl":"/images/icons/prem_user.gif"}
	,"Images":[]}
 */
}
