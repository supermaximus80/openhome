/* PluginDetect v0.5.0 [ Java(OTF) QT ] by Eric Gerds www.pinlady.net/PluginDetect */ 
if(!PluginDetect){
var PluginDetect={getNum:function(A,_2){
if(!this.num(A)){
return null;
}
var m;
if(typeof _2=="undefined"){
m=/[\d][\d\.\_,-]*/.exec(A);
}else{
m=(new RegExp(_2)).exec(A);
}
return m?m[0].replace(/[\.\_-]/g,","):null;
},hasMimeType:function(_4){
if(PluginDetect.isIE){
return null;
}
var s,t,z,M=_4.constructor==String?[_4]:_4;
for(z=0;z<M.length;z++){
s=navigator.mimeTypes[M[z]];
if(s&&s.enabledPlugin){
t=s.enabledPlugin;
if(t.name||t.description){
return s;
}
}
}
return null;
},findNavPlugin:function(N,_7){
var _8=N.constructor==String?N:N.join(".*"),numS=_7===false?"":"\\d";
var i,re=new RegExp(_8+".*"+numS+"|"+numS+".*"+_8,"i");
var _a=navigator.plugins;
for(i=0;i<_a.length;i++){
if(re.test(_a[i].description)||re.test(_a[i].name)){
return _a[i];
}
}
return null;
},getAXO:function(_b){
var _c,e;
try{
_c=new ActiveXObject(_b);
return _c;
}
catch(e){
}
return null;
},num:function(A){
return (typeof A!="string"?false:(/\d/).test(A));
},compareNums:function(_e,_f){
if(!this.num(_e)||!this.num(_f)){
return 0;
}
if(this.plugin&&this.plugin.compareNums){
return this.plugin.compareNums(_e,_f);
}
var m1=_e.split(","),m2=_f.split(","),x,p=parseInt;
for(x=0;x<Math.min(m1.length,m2.length);x++){
if(p(m1[x],10)>p(m2[x],10)){
return 1;
}
if(p(m1[x],10)<p(m2[x],10)){
return -1;
}
}
return 0;
},formatNum:function(num){
if(!this.num(num)){
return null;
}
var x,n=num.replace(/\s/g,"").replace(/[\.\_]/g,",").split(",").concat(["0","0","0","0"]);
for(x=0;x<4;x++){
if(/^(0+)(.+)$/.test(n[x])){
n[x]=RegExp.$2;
}
}
return n[0]+","+n[1]+","+n[2]+","+n[3];
},initScript:function(){
var $=this,IE;
$.isIE=/*@cc_on!@*/false;
$.IEver=-1;
$.ActiveXEnabled=false;
if($.isIE){
IE=(/msie\s*\d\.{0,1}\d*/i).exec(navigator.userAgent);
if(IE){
$.IEver=parseFloat((/\d.{0,1}\d*/i).exec(IE[0]),10);
}
var _14,x;
_14=["ShockwaveFlash.ShockwaveFlash","Msxml2.XMLHTTP","Microsoft.XMLDOM","Msxml2.DOMDocument","TDCCtl.TDCCtl","Shell.UIHelper","Scripting.Dictionary","wmplayer.ocx"];
for(x=0;x<_14.length;x++){
if($.getAXO(_14[x])){
$.ActiveXEnabled=true;
break;
}
}
}
if($.isIE){
$.head=typeof document.getElementsByTagName!="undefined"?document.getElementsByTagName("head")[0]:null;
}
},init:function(_15){
if(typeof _15!="string"){
return -3;
}
_15=_15.toLowerCase().replace(/\s/g,"");
var $=this,IE,p;
if(typeof $[_15]=="undefined"){
return -3;
}
p=$[_15];
$.plugin=p;
if(typeof p.installed=="undefined"){
p.installed=null;
p.version=null;
p.getVersionDone=null;
}
$.garbage=false;
if($.isIE&&!$.ActiveXEnabled){
return -2;
}
return 1;
},isMinVersion:function(_17,_18,_19){
;
var $=PluginDetect,i=$.init(_17);
if(i<0){
return i;
}
if(typeof _18=="undefined"||_18==null){
_18="0";
}
if(typeof _18=="number"){
_18=_18.toString();
}
if(!$.num(_18)){
return -3;
}
_18=$.formatNum(_18);
if(typeof _19=="undefined"){
_19=null;
}
var _1b=-1,p=$.plugin;
if(p.getVersionDone!=1){
;
;
p.getVersion(_18,_19);
if(p.getVersionDone==null||p.version!=null){
p.getVersionDone=1;
}
}
if(p.version!=null||p.installed!=null){
if(p.installed<=0.5){
_1b=p.installed;
}else{
_1b=(p.version==null?0:($.compareNums(p.version,_18)>=0?1:-1));
}
}
$.cleanup();
return _1b;
;
return -3;
},getVersion:function(_1c,_1d){
;
var $=PluginDetect,i=$.init(_1c);
if(i<0){
return null;
}
var p=$.plugin;
if(typeof _1d=="undefined"){
_1d=null;
}
if(p.getVersionDone!=1){
p.getVersion(null,_1d);
if(p.getVersionDone==null||p.version!=null){
p.getVersionDone=1;
}
}
$.cleanup();
return p.version;
;
return null;
},cleanup:function(){
;
var $=this;
if($.garbage&&typeof window.CollectGarbage!="undefined"){
window.CollectGarbage();
}
;
},isActiveXObject:function(_21){
;
var $=this,result,e,s="<object width=\"1\" height=\"1\" "+"style=\"display:none\" "+$.plugin.getCodeBaseVersion(_21)+">"+$.plugin.HTML+"</object>";
if($.head.firstChild){
$.head.insertBefore(document.createElement("object"),$.head.firstChild);
}else{
$.head.appendChild(document.createElement("object"));
}
$.head.firstChild.outerHTML=s;
try{
$.head.firstChild.classid=$.plugin.classID;
}
catch(e){
}
result=false;
try{
if($.head.firstChild.object){
result=true;
}
}
catch(e){
}
try{
if(result&&$.head.firstChild.readyState<4){
$.garbage=true;
}
}
catch(e){
}
$.head.removeChild($.head.firstChild);
return result;
;
},codebaseSearch:function(min){
var $=this;
if(typeof min!="undefined"){
return $.isActiveXObject(min);
}
;
var _25=[0,0,0,0],x,y,A=$.plugin.digits,t=function(x,y){
var _28=(x==0?y:_25[0])+","+(x==1?y:_25[1])+","+(x==2?y:_25[2])+","+(x==3?y:_25[3]);
return $.isActiveXObject(_28);
};
var _29,tmp;
var _2a=false;
for(x=0;x<A.length;x++){
_29=A[x]*2;
_25[x]=0;
for(y=0;y<20;y++){
if(_29==1&&x>0&&_2a){
break;
}
if(_29-_25[x]>1){
tmp=Math.round((_29+_25[x])/2);
if(t(x,tmp)){
_25[x]=tmp;
_2a=true;
}else{
_29=tmp;
}
}else{
if(_29-_25[x]==1){
_29--;
if(!_2a&&t(x,_29)){
_2a=true;
}
break;
}else{
if(!_2a&&t(x,_29)){
_2a=true;
}
break;
}
}
}
if(!_2a){
return null;
}
}
return _25.join(",");
;
},dummy1:0};
}
PluginDetect.initScript();
PluginDetect.onJavaDetectionDone=function(f,jar){
var $=this,j=$.java,z;
if(j.getVersionDone!=1){
z=$.isMinVersion("Java","0",jar);
if(z==-3){
z=$.getVersion("Java",jar);
}
}
if(j.installed!=null&&j.installed!=-0.5&&j.installed!=0.5){
if(typeof f=="function"){
f();
}
return;
}
;
};
PluginDetect.onWindowLoaded=function(f){
;
var $=this,w=window;
if($.EventWinLoad===true){
}else{
$.EventWinLoad=true;
if(typeof w.addEventListener!="undefined"){
w.addEventListener("load",$.runFuncs,false);
}else{
if(typeof w.attachEvent!="undefined"){
w.attachEvent("onload",$.runFuncs);
}else{
if(typeof w.onload=="function"){
$.funcs[$.funcs.length]=w.onload;
}
w.onload=$.runFuncs;
}
}
}
if(typeof f=="function"){
$.funcs[$.funcs.length]=f;
}
;
};
;
PluginDetect.funcs=[0];
PluginDetect.runFuncs=function(){
var $=PluginDetect,x;
for(x=0;x<$.funcs.length;x++){
if(typeof $.funcs[x]=="function"){
$.funcs[x]();
}
}
};
;
PluginDetect.quicktime={mimeType:["video/quicktime","application/x-quicktimeplayer","image/x-macpaint","image/x-quicktime"],progID:"QuickTimeCheckObject.QuickTimeCheck.1",progID0:"QuickTime.QuickTime",classID:"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B",minIEver:7,HTML:"<param name=\"src\" value=\"A14999.mov\" /><param name=\"controller\" value=\"false\" />",getCodeBaseVersion:function(v){
var r=v.replace(/[\.\_]/g,",").split(","),$=PluginDetect;
if($.compareNums(v,"7,5,0,0")>=0){
v=r[0]+","+r[1]+r[2]+","+r[3];
}
return "codebase=\"#version="+v+"\"";
},digits:[16,16,16,0],clipTo3digits:function(v){
if(v==null||typeof v=="undefined"){
return null;
}
var t;
t=v.split(",");
return t[0]+","+t[1]+","+t[2]+",0";
},getVersion:function(){
var _35=null,p,$=PluginDetect;
var _36=true;
if(!$.isIE){
if(navigator.platform&&(/linux/i).test(navigator.platform)){
_36=false;
}
if(_36){
p=$.findNavPlugin(["QuickTime","(Plug-in|Plugin)"]);
if(p&&p.name&&$.hasMimeType(this.mimeType)){
_35=$.getNum(p.name);
}
}
this.installed=_35?1:-1;
}else{
var obj;
if($.IEver>=this.minIEver&&$.getAXO(this.progID0)){
_35=$.codebaseSearch();
}else{
obj=$.getAXO(this.progID);
if(obj&&obj.QuickTimeVersion){
_35=obj.QuickTimeVersion.toString(16);
_35=_35.charAt(0)+"."+_35.charAt(1)+"."+_35.charAt(2);
}
}
this.installed=_35?1:($.getAXO(this.progID0)?0:-1);
}
this.version=this.clipTo3digits($.formatNum(_35));
}};
;
PluginDetect.java={mimeType:"application/x-java-applet",classID:"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93",DTKclassID:"clsid:CAFEEFAC-DEC7-0000-0000-ABCDEFFEDCBA",DTKmimeType:"application/npruntime-scriptable-plugin;DeploymentToolkit",minWebStart:"1,4,2,0",JavaVersions:["1,9,1,25","1,8,1,25","1,7,1,25","1,6,1,25","1,5,0,25","1,4,2,25","1,3,1,25"],lowestPreApproved:"1,6,0,02",lowestSearchable:"1,3,1,0",searchAXOJavaPlugin:function(min,_39){
var e,z,T,$=PluginDetect;
var _3b,C_DE,C,DE,v;
var AXO=ActiveXObject;
var _3d=(typeof _39!="undefined")?_39:this.minWebStart;
var Q=min.split(","),x;
for(x=0;x<4;x++){
Q[x]=parseInt(Q[x],10);
}
for(x=0;x<3;x++){
if(Q[x]>9){
Q[x]=9;
}
}
if(Q[3]>99){
Q[3]=99;
}
var _3f="JavaPlugin."+Q[0]+Q[1]+Q[2]+(Q[3]>0?("_"+(Q[3]<10?"0":"")+Q[3]):"");
for(z=0;z<this.JavaVersions.length;z++){
if($.compareNums(min,this.JavaVersions[z])>0){
return null;
}
T=this.JavaVersions[z].split(",");
_3b="JavaPlugin."+T[0]+T[1];
v=T[0]+"."+T[1]+".";
for(C=T[2];C>=0;C--){
if($.compareNums(T[0]+","+T[1]+","+C+",0",_3d)>=0){
try{
new AXO("JavaWebStart.isInstalled."+v+C+".0");
}
catch(e){
continue;
}
}
if($.compareNums(min,T[0]+","+T[1]+","+C+","+T[3])>0){
return null;
}
for(DE=T[3];DE>=0;DE--){
C_DE=C+"_"+(DE<10?"0"+DE:DE);
try{
new AXO(_3b+C_DE);
return v+C_DE;
}
catch(e){
}
if(_3b+C_DE==_3f){
return null;
}
}
try{
new AXO(_3b+C);
return v+C;
}
catch(e){
}
if(_3b+C==_3f){
return null;
}
}
}
return null;
},minIEver:7,HTML:"<param name=\"code\" value=\"A14999.class\" />",getCodeBaseVersion:function(v){
var r=v.replace(/[\.\_]/g,",").split(","),$=PluginDetect;
if($.compareNums(v,"1,4,1,02")<0){
v=r[0]+","+r[1]+","+r[2]+","+r[3];
}else{
if($.compareNums(v,"1,5,0,02")<0){
v=r[0]+","+r[1]+","+r[2]+","+r[3]+"0";
}else{
v=Math.round((parseFloat(r[0]+"."+r[1],10)-1.5)*10+5)+","+r[2]+","+r[3]+"0"+",0";
}
}
return "codebase=\"#version="+v+"\"";
},digits:[2,8,8,32],getFromMimeType:function(_42){
var x,t,$=PluginDetect;
var re=new RegExp(_42);
var tmp,v="0,0,0,0",digits="";
for(x=0;x<navigator.mimeTypes.length;x++){
t=navigator.mimeTypes[x];
if(re.test(t.type)&&t.enabledPlugin){
t=t.type.substring(t.type.indexOf("=")+1,t.type.length);
tmp=$.formatNum(t);
if($.compareNums(tmp,v)>0){
v=tmp;
digits=t;
}
}
}
return digits.replace(/[\.\_]/g,",");
},hasRun:false,value:null,queryJavaHandler:function(){
var $=PluginDetect.java,j=window.java,e;
$.hasRun=true;
try{
if(typeof j.lang!="undefined"&&typeof j.lang.System!="undefined"){
$.value=j.lang.System.getProperty("java.version")+" ";
}
}
catch(e){
}
},queryJava:function(){
var $=PluginDetect,t=this,nua=navigator.userAgent,e;
if(typeof window.java!="undefined"&&window.navigator.javaEnabled()){
if(/gecko/i.test(nua)){
if($.hasMimeType("application/x-java-vm")){
try{
var div=document.createElement("div"),evObj=document.createEvent("HTMLEvents");
evObj.initEvent("focus",false,true);
div.addEventListener("focus",t.queryJavaHandler,false);
div.dispatchEvent(evObj);
}
catch(e){
}
if(!t.hasRun){
t.queryJavaHandler();
}
}
}else{
if(/opera.9\.(0|1)/i.test(nua)&&/mac/i.test(nua)){
return null;
}
t.queryJavaHandler();
}
}
return t.value;
},getVersion:function(min,jar){
if(typeof min=="undefined"){
min=null;
}
if(typeof jar=="undefined"){
jar=null;
}
var _4b=null,$=PluginDetect,tmp;
if(this.getVersionDone==0){
tmp=this.queryExternalApplet(jar);
if(tmp[0]){
_4b=tmp[0];
}
this.EndGetVersion(_4b);
return;
}
var dtk=this.queryDeploymentToolKit();
if(dtk==-1&&$.isIE){
this.installed=-1;
return;
}
if(dtk!=-1&&dtk!=null){
_4b=dtk;
}
if(!$.isIE){
var p1,p2,p;
var _4e,mt;
mt=$.hasMimeType(this.mimeType);
_4e=(mt&&navigator.javaEnabled());
if(!_4b&&_4e){
tmp="Java[^\\d]*Plug-in";
p=$.findNavPlugin(tmp);
if(p){
tmp=new RegExp(tmp,"i");
p1=tmp.test(p.description)?$.getNum(p.description):null;
p2=tmp.test(p.name)?$.getNum(p.name):null;
if(p1&&p2){
_4b=($.compareNums($.formatNum(p1),$.formatNum(p2))>=0)?p1:p2;
}else{
_4b=p1||p2;
}
}
}
if(!_4b&&(_4e||(mt&&/linux/i.test(navigator.userAgent)&&$.findNavPlugin("IcedTea.*Java",false)))){
tmp=this.getFromMimeType("application/x-java-applet.*jpi-version.*=");
if(tmp!=""){
_4b=tmp;
}
}
if(!_4b&&_4e&&/macintosh.*safari/i.test(navigator.userAgent)){
p=$.findNavPlugin("Java.*\\d.*Plug-in.*Cocoa",false);
if(p){
p1=$.getNum(p.description);
if(p1){
_4b=p1;
}
}
}
if(!_4b){
p=this.queryJava();
if(p){
_4b=p;
}
}
if(!_4b){
p=this.queryExternalApplet(jar);
if(p[0]){
_4b=p[0];
}
}
if(this.installed==null&&!_4b&&_4e&&!/macintosh.*ppc/i.test(navigator.userAgent)){
tmp=this.getFromMimeType("application/x-java-applet.*version.*=");
if(tmp!=""){
_4b=tmp;
}
}
if(!_4b&&_4e){
if(/macintosh.*safari/i.test(navigator.userAgent)){
if(this.installed==null){
this.installed=0;
}else{
if(this.installed==-0.5){
this.installed=0.5;
}
}
}
}
if(this.installed==null){
this.installed=_4b?1:-1;
}
}else{
var Q;
if(!_4b){
if($.IEver>=this.minIEver){
Q=this.findMax(this.lowestPreApproved,min);
_4b=this.searchAXOJavaPlugin(Q,this.lowestPreApproved);
}else{
Q=this.findMax(this.lowestSearchable,min);
_4b=this.searchAXOJavaPlugin(Q);
}
}
if(!_4b){
this.JavaFix();
}
if(!_4b){
tmp=this.queryExternalApplet(jar);
if(tmp[0]){
_4b=tmp[0];
}
}
if(!_4b&&$.IEver>=this.minIEver){
_4b=$.codebaseSearch();
}
this.installed=_4b?1:(this.installed==null?-1:this.installed);
}
this.EndGetVersion(_4b);
},EndGetVersion:function(_50){
this.setVersion(_50);
if(!_50&&typeof this.queryExternalAppletResult=="undefined"){
this.getVersionDone=0;
}else{
this.getVersionDone=1;
}
},findMax:function(_51,_52){
var $=PluginDetect;
if(typeof _52=="undefined"||_52==null||$.compareNums(_52,_51)<0){
return _51;
}
return _52;
},setVersion:function(_54){
var $=PluginDetect;
this.version=$.formatNum($.getNum(_54));
if(typeof this.version=="string"&&this.allVersions.length==0){
this.allVersions[0]=this.version;
}
},allVersions:[],queryDeploymentToolKit:function(){
if(typeof this.queryDTKresult!="undefined"){
return this.queryDTKresult;
}
this.allVersions=[];
var $=PluginDetect,e,x;
var _57=[null,null],obj;
var len=null;
if($.isIE&&$.IEver>=6){
_57=$.instantiate("object","","");
}
if(!$.isIE&&$.hasMimeType(this.DTKmimeType)){
_57=$.instantiate("object","type="+this.DTKmimeType,"");
}
if(_57[0]&&_57[1]&&_57[1].parentNode){
obj=_57[0].firstChild;
if($.isIE&&$.IEver>=6){
try{
obj.classid=this.DTKclassID;
}
catch(e){
}
try{
if(obj.object&&obj.readyState<4){
$.garbage=true;
}
}
catch(e){
}
}
try{
len=obj.jvms.getLength();
if(len!=null&&len>0){
for(x=0;x<len;x++){
this.allVersions[x]=$.formatNum($.getNum(obj.jvms.get(x).version));
}
}
}
catch(e){
}
_57[1].parentNode.removeChild(_57[1]);
}
this.queryDTKresult=this.allVersions.length>0?this.allVersions[this.allVersions.length-1]:(len==0?-1:null);
return this.queryDTKresult;
},queryExternalApplet:function(jar){
var $=PluginDetect,t=this;
var _5b=[null,null];
if(typeof t.queryExternalAppletResult!="undefined"){
return t.queryExternalAppletResult;
}
if(!jar||typeof jar!="string"||!(/\.jar\s*$/).test(jar)||(!$.isIE&&!$.hasMimeType(t.mimeType))){
return [null,null];
}
var par="<param name=\"archive\" value=\""+jar+"\" />"+"<param name=\"mayscript\" value=\"true\" />"+"<param name=\"scriptable\" value=\"true\" />";
var _5d=function(_5e){
;
};
t.C[0]=$.isIE?$.instantiate("object","archive=\""+jar+"\" code=\"A.class\" type=\""+t.mimeType+"\"","<param name=\"code\" value=\"A.class\" />"+par):$.instantiate("object","archive=\""+jar+"\" classid=\"java:A.class\" type=\""+t.mimeType+"\"",par);
_5b=t.query1Applet(0);
_5d(0);
if(!_5b[0]&&!$.isIE){
var _61=document.createElement("span");
t.C[0][0].appendChild(_61);
t.C[1]=$.instantiate("applet","archive=\""+jar+"\" code=\"A.class\" mayscript=\"true\"","<param name=\"mayscript\" value=\"true\">",_61);
_5b=t.query1Applet(1);
}
;
$.onWindowLoaded(t.delJavaApplets);
t.queryExternalAppletResult=[_5b[0],_5b[1]];
return t.queryExternalAppletResult;
},C:[[null,null],[null,null]],delJavaApplets:function(){
var C=PluginDetect.java.C,c,x;
for(x=1;x>=0;x--){
c=C[x];
if(c[0]&&c[0].firstChild){
c[0].removeChild(c[0].firstChild);
}
if(c[1]&&c[1].parentNode){
c[1].parentNode.removeChild(c[1]);
}
C[x]=[null,null];
}
},query1Applet:function(_63){
var e,$=PluginDetect,j=$.java,c=j.C[_63][0],Z=[null,null];
var _65=c&&c.firstChild?c.firstChild:null;
try{
if(_65){
Z=[_65.getVersion()+" ",_65.getVendor()+" "];
if($.isIE&&Z[0]&&_65.readyState!=4){
$.garbage=true;
j.delJavaApplets();
}
}
}
catch(e){
}
return Z;
},funcs:[],NOTF:{SetupAppletQuery:function(){
;
}},JavaFix:function(){
}};
;
PluginDetect.instantiate=function(_76,_77,_78,div){
var s=function(_7b){
var c=_7b.style;
c.border="0px";
c.width="1px";
c.height="1px";
c.padding="0px";
c.margin="0px";
c.visibility="hidden";
};
var e,d=document,tag1="<"+_76+" width=\"1\" height=\"1\" "+_77+">"+_78+"</"+_76+">",body=(d.getElementsByTagName("body")[0]||d.body);
if(typeof div=="undefined"){
div=d.createElement("div");
if(body){
body.appendChild(div);
}else{
try{
d.write("<div>o</div><div>"+tag1+"</div>");
body=(d.getElementsByTagName("body")[0]||d.body);
body.removeChild(body.firstChild);
div=body.firstChild;
}
catch(e){
try{
body=d.createElement("body");
d.getElementsByTagName("html")[0].appendChild(body);
body.appendChild(div);
div.innerHTML=tag1;
s(div);
return [div,body];
}
catch(e){
}
}
s(div);
return [div,div];
}
}
if(div&&div.parentNode){
try{
div.innerHTML=tag1;
}
catch(e){
}
}
s(div);
return [div,div];
};
