/*
 * This is a class GENERATED by the TransportGenerator maven plugin. DON'T MODIFY IT.
 * IF you modify it, your work may be lost: the class will be overwritten automatically
 * when the maven plugin is re-executed for any reasons.
 */
package de.fhg.iais.roberta.generated.restEntities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * the response for the /setRobot REST request<br><br>
 * Version: 1<br>
 * Datum: 2020-06-15
 */
public class SetRobotResponse extends BaseResponse {
    protected String robot;
    protected JSONObject program;
    protected JSONObject configuration;
    protected boolean sim;
    protected boolean simDefined = false;
    protected boolean multipleSim;
    protected boolean multipleSimDefined = false;
    protected boolean markerSim;
    protected boolean markerSimDefined = false;
    protected boolean nn;
    protected boolean nnDefined = false;
    protected JSONArray availableNNActivationFunctions;
    protected boolean webotsSim;
    protected boolean webotsSimDefined = false;
    protected String webotsUrl;
    protected String connection;
    protected String vendor;
    protected boolean configurationUsed;
    protected boolean configurationUsedDefined = false;
    protected String commandLine;
    protected String signature;
    protected String sourceCodeFileExtension;
    protected String binaryFileExtension;
    protected boolean hasWlan;
    protected boolean hasWlanDefined = false;
    protected String firmwareDefault;
    
    /**
     * the response for the /setRobot REST request
     */
    public static SetRobotResponse make() {
        return new SetRobotResponse();
    }
    
    /**
     * the response for the /setRobot REST request
     */
    public static SetRobotResponse makeFromString(String jsonS) {
        try {
            JSONObject jsonO = new JSONObject(jsonS);
            return make(jsonO);
        } catch (JSONException e) {
            throw new RuntimeException("JSON parse error when parsing: " + jsonS, e);
        }
    }
    
    /**
     * the response for the /setRobot REST request
     */
    public static SetRobotResponse makeFromProperties(String cmd,String rc,String message,String cause,JSONObject parameters,String initToken,long serverTime,String serverVersion,long robotWait,String robotBattery,String robotName,String robotVersion,String robotFirmwareName,JSONObject robotSensorvalues,int robotNepoexitvalue,String robotState,boolean notificationsAvailable,String robot,JSONObject program,JSONObject configuration,boolean sim,boolean multipleSim,boolean markerSim,boolean nn,JSONArray availableNNActivationFunctions,boolean webotsSim,String webotsUrl,String connection,String vendor,boolean configurationUsed,String commandLine,String signature,String sourceCodeFileExtension,String binaryFileExtension,boolean hasWlan,String firmwareDefault) {
        SetRobotResponse entity = new SetRobotResponse();
        entity.setCmd(cmd);
        entity.setRc(rc);
        entity.setMessage(message);
        entity.setCause(cause);
        entity.setParameters(parameters);
        entity.setInitToken(initToken);
        entity.setServerTime(serverTime);
        entity.setServerVersion(serverVersion);
        entity.setRobotWait(robotWait);
        entity.setRobotBattery(robotBattery);
        entity.setRobotName(robotName);
        entity.setRobotVersion(robotVersion);
        entity.setRobotFirmwareName(robotFirmwareName);
        entity.setRobotSensorvalues(robotSensorvalues);
        entity.setRobotNepoexitvalue(robotNepoexitvalue);
        entity.setRobotState(robotState);
        entity.setNotificationsAvailable(notificationsAvailable);
        entity.setRobot(robot);
        entity.setProgram(program);
        entity.setConfiguration(configuration);
        entity.setSim(sim);
        entity.setMultipleSim(multipleSim);
        entity.setMarkerSim(markerSim);
        entity.setNn(nn);
        entity.setAvailableNNActivationFunctions(availableNNActivationFunctions);
        entity.setWebotsSim(webotsSim);
        entity.setWebotsUrl(webotsUrl);
        entity.setConnection(connection);
        entity.setVendor(vendor);
        entity.setConfigurationUsed(configurationUsed);
        entity.setCommandLine(commandLine);
        entity.setSignature(signature);
        entity.setSourceCodeFileExtension(sourceCodeFileExtension);
        entity.setBinaryFileExtension(binaryFileExtension);
        entity.setHasWlan(hasWlan);
        entity.setFirmwareDefault(firmwareDefault);
        entity.immutable();
        return entity;
    }
    
    /**
     * the response for the /setRobot REST request
     */
    public static SetRobotResponse make(JSONObject jsonO) {
        return make().merge(jsonO).immutable();
    }
    
    /**
     * merge the properties of a JSON-object into this bean. The bean must be "under construction".
     * The keys of the JSON-Object must be valid. The bean remains "under construction".<br>
     * Throws a runtime exception if inconsistencies are detected.
     */
    public SetRobotResponse merge(JSONObject jsonO) {
        try {
            for (String key : JSONObject.getNames(jsonO)) {
                if ("_version".equals(key)) {
                } else if ("cmd".equals(key)) {
                    setCmd(jsonO.optString(key));
                } else if ("rc".equals(key)) {
                    setRc(jsonO.getString(key));
                } else if ("message".equals(key)) {
                    setMessage(jsonO.optString(key));
                } else if ("cause".equals(key)) {
                    setCause(jsonO.optString(key));
                } else if ("parameters".equals(key)) {
                    setParameters(jsonO.optJSONObject(key));
                } else if ("initToken".equals(key)) {
                    setInitToken(jsonO.getString(key));
                } else if ("server.time".equals(key)) {
                    setServerTime(jsonO.getLong(key));
                } else if ("server.version".equals(key)) {
                    setServerVersion(jsonO.getString(key));
                } else if ("robot.wait".equals(key)) {
                    setRobotWait(jsonO.optLong(key));
                } else if ("robot.battery".equals(key)) {
                    setRobotBattery(jsonO.optString(key));
                } else if ("robot.name".equals(key)) {
                    setRobotName(jsonO.optString(key));
                } else if ("robot.version".equals(key)) {
                    setRobotVersion(jsonO.optString(key));
                } else if ("robot.firmwareName".equals(key)) {
                    setRobotFirmwareName(jsonO.optString(key));
                } else if ("robot.sensorvalues".equals(key)) {
                    setRobotSensorvalues(jsonO.optJSONObject(key));
                } else if ("robot.nepoexitvalue".equals(key)) {
                    setRobotNepoexitvalue(jsonO.optInt(key));
                } else if ("robot.state".equals(key)) {
                    setRobotState(jsonO.optString(key));
                } else if ("notifications.available".equals(key)) {
                    setNotificationsAvailable(jsonO.optBoolean(key));
                } else if ("robot".equals(key)) {
                    setRobot(jsonO.getString(key));
                } else if ("program".equals(key)) {
                    setProgram(jsonO.getJSONObject(key));
                } else if ("configuration".equals(key)) {
                    setConfiguration(jsonO.getJSONObject(key));
                } else if ("sim".equals(key)) {
                    setSim(jsonO.getBoolean(key));
                } else if ("multipleSim".equals(key)) {
                    setMultipleSim(jsonO.getBoolean(key));
                } else if ("markerSim".equals(key)) {
                    setMarkerSim(jsonO.getBoolean(key));
                } else if ("nn".equals(key)) {
                    setNn(jsonO.getBoolean(key));
                } else if ("availableNNActivationFunctions".equals(key)) {
                    setAvailableNNActivationFunctions(jsonO.getJSONArray(key));
                } else if ("webotsSim".equals(key)) {
                    setWebotsSim(jsonO.getBoolean(key));
                } else if ("webotsUrl".equals(key)) {
                    setWebotsUrl(jsonO.optString(key));
                } else if ("connection".equals(key)) {
                    setConnection(jsonO.getString(key));
                } else if ("vendor".equals(key)) {
                    setVendor(jsonO.getString(key));
                } else if ("configurationUsed".equals(key)) {
                    setConfigurationUsed(jsonO.getBoolean(key));
                } else if ("commandLine".equals(key)) {
                    setCommandLine(jsonO.optString(key));
                } else if ("signature".equals(key)) {
                    setSignature(jsonO.optString(key));
                } else if ("sourceCodeFileExtension".equals(key)) {
                    setSourceCodeFileExtension(jsonO.getString(key));
                } else if ("binaryFileExtension".equals(key)) {
                    setBinaryFileExtension(jsonO.getString(key));
                } else if ("hasWlan".equals(key)) {
                    setHasWlan(jsonO.getBoolean(key));
                } else if ("firmwareDefault".equals(key)) {
                    setFirmwareDefault(jsonO.optString(key));
                } else {
                    throw new RuntimeException("JSON parse error. Found invalid key: " + key + " in " + jsonO);
                }
            }
            return this;
        } catch (Exception e) {
            throw new RuntimeException("JSON parse / casting error when parsing: " + jsonO, e);
        }
    }
    
    /**
     * moves a bean from state "under construction" to state "immutable".<br>
     * Checks whether all required fields are set. All lists are made immutable.<br>
     * Throws a runtime exception if inconsistencies are detected.
     */
    public SetRobotResponse immutable() {
        if (this.immutable) {
            return this;
        }
        this.immutable = true;
        return validate();
    }
    
    /**
     * Checks whether all required fields are set.<br>
     * Throws a runtime exception if inconsistencies are detected.
     */
    private SetRobotResponse validate() {
        String _message = null;
        if ( !this.immutable ) {
            _message = "SetRobotResponse-object is already immutable: " + toString();
        }
        if ( rc == null) {
            _message = "required property rc of SetRobotResponse-object is not set: " + toString();
        }
        if ( initToken == null) {
            _message = "required property initToken of SetRobotResponse-object is not set: " + toString();
        }
        if ( !serverTimeDefined) {
            _message = "required property serverTime of SetRobotResponse-object is not set: " + toString();
        }
        if ( serverVersion == null) {
            _message = "required property serverVersion of SetRobotResponse-object is not set: " + toString();
        }
        if ( robot == null) {
            _message = "required property robot of SetRobotResponse-object is not set: " + toString();
        }
        if ( program == null) {
            _message = "required property program of SetRobotResponse-object is not set: " + toString();
        }
        if ( configuration == null) {
            _message = "required property configuration of SetRobotResponse-object is not set: " + toString();
        }
        if ( !simDefined) {
            _message = "required property sim of SetRobotResponse-object is not set: " + toString();
        }
        if ( !multipleSimDefined) {
            _message = "required property multipleSim of SetRobotResponse-object is not set: " + toString();
        }
        if ( !markerSimDefined) {
            _message = "required property markerSim of SetRobotResponse-object is not set: " + toString();
        }
        if ( !nnDefined) {
            _message = "required property nn of SetRobotResponse-object is not set: " + toString();
        }
        if ( availableNNActivationFunctions == null) {
            _message = "required property availableNNActivationFunctions of SetRobotResponse-object is not set: " + toString();
        }
        if ( !webotsSimDefined) {
            _message = "required property webotsSim of SetRobotResponse-object is not set: " + toString();
        }
        if ( connection == null) {
            _message = "required property connection of SetRobotResponse-object is not set: " + toString();
        }
        if ( vendor == null) {
            _message = "required property vendor of SetRobotResponse-object is not set: " + toString();
        }
        if ( !configurationUsedDefined) {
            _message = "required property configurationUsed of SetRobotResponse-object is not set: " + toString();
        }
        if ( sourceCodeFileExtension == null) {
            _message = "required property sourceCodeFileExtension of SetRobotResponse-object is not set: " + toString();
        }
        if ( binaryFileExtension == null) {
            _message = "required property binaryFileExtension of SetRobotResponse-object is not set: " + toString();
        }
        if ( !hasWlanDefined) {
            _message = "required property hasWlan of SetRobotResponse-object is not set: " + toString();
        }
        if ( _message != null ) {
            this.immutable = false;
            throw new RuntimeException(_message);
        }
        return this;
    }
    
    /**
     * GET robot. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getRobot() {
        if (!this.immutable) {
            throw new RuntimeException("no robot from an object under construction: " + toString());
        }
        return this.robot;
    }
    
    /**
     * SET robot. Object must be mutable.
     */
    public SetRobotResponse setRobot(String robot) {
        if (this.immutable) {
            throw new RuntimeException("robot assigned to an immutable object: " + toString());
        }
        this.robot = robot;
        return this;
    }
    
    /**
     * GET program. Object must be immutable. Never return null or an undefined/default value.
     */
    public JSONObject getProgram() {
        if (!this.immutable) {
            throw new RuntimeException("no program from an object under construction: " + toString());
        }
        return this.program;
    }
    
    /**
     * SET program. Object must be mutable.
     */
    public SetRobotResponse setProgram(JSONObject program) {
        if (this.immutable) {
            throw new RuntimeException("program assigned to an immutable object: " + toString());
        }
        this.program = program;
        return this;
    }
    
    /**
     * GET configuration. Object must be immutable. Never return null or an undefined/default value.
     */
    public JSONObject getConfiguration() {
        if (!this.immutable) {
            throw new RuntimeException("no configuration from an object under construction: " + toString());
        }
        return this.configuration;
    }
    
    /**
     * SET configuration. Object must be mutable.
     */
    public SetRobotResponse setConfiguration(JSONObject configuration) {
        if (this.immutable) {
            throw new RuntimeException("configuration assigned to an immutable object: " + toString());
        }
        this.configuration = configuration;
        return this;
    }
    
    /**
     * GET sim. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getSim() {
        if (!this.immutable) {
            throw new RuntimeException("no sim from an object under construction: " + toString());
        }
        return this.sim;
    }
    
    /**
     * SET sim. Object must be mutable.
     */
    public SetRobotResponse setSim(boolean sim) {
        if (this.immutable) {
            throw new RuntimeException("sim assigned to an immutable object: " + toString());
        }
        this.sim = sim;
        this.simDefined = true;
        return this;
    }
    
    /**
     * GET multipleSim. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getMultipleSim() {
        if (!this.immutable) {
            throw new RuntimeException("no multipleSim from an object under construction: " + toString());
        }
        return this.multipleSim;
    }
    
    /**
     * SET multipleSim. Object must be mutable.
     */
    public SetRobotResponse setMultipleSim(boolean multipleSim) {
        if (this.immutable) {
            throw new RuntimeException("multipleSim assigned to an immutable object: " + toString());
        }
        this.multipleSim = multipleSim;
        this.multipleSimDefined = true;
        return this;
    }
    
    /**
     * GET markerSim. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getMarkerSim() {
        if (!this.immutable) {
            throw new RuntimeException("no markerSim from an object under construction: " + toString());
        }
        return this.markerSim;
    }
    
    /**
     * SET markerSim. Object must be mutable.
     */
    public SetRobotResponse setMarkerSim(boolean markerSim) {
        if (this.immutable) {
            throw new RuntimeException("markerSim assigned to an immutable object: " + toString());
        }
        this.markerSim = markerSim;
        this.markerSimDefined = true;
        return this;
    }
    
    /**
     * GET nn. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getNn() {
        if (!this.immutable) {
            throw new RuntimeException("no nn from an object under construction: " + toString());
        }
        return this.nn;
    }
    
    /**
     * SET nn. Object must be mutable.
     */
    public SetRobotResponse setNn(boolean nn) {
        if (this.immutable) {
            throw new RuntimeException("nn assigned to an immutable object: " + toString());
        }
        this.nn = nn;
        this.nnDefined = true;
        return this;
    }
    
    /**
     * GET availableNNActivationFunctions. Object must be immutable. Never return null or an undefined/default value.
     */
    public JSONArray getAvailableNNActivationFunctions() {
        if (!this.immutable) {
            throw new RuntimeException("no availableNNActivationFunctions from an object under construction: " + toString());
        }
        return this.availableNNActivationFunctions;
    }
    
    /**
     * SET availableNNActivationFunctions. Object must be mutable.
     */
    public SetRobotResponse setAvailableNNActivationFunctions(JSONArray availableNNActivationFunctions) {
        if (this.immutable) {
            throw new RuntimeException("availableNNActivationFunctions assigned to an immutable object: " + toString());
        }
        this.availableNNActivationFunctions = availableNNActivationFunctions;
        return this;
    }
    
    /**
     * GET webotsSim. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getWebotsSim() {
        if (!this.immutable) {
            throw new RuntimeException("no webotsSim from an object under construction: " + toString());
        }
        return this.webotsSim;
    }
    
    /**
     * SET webotsSim. Object must be mutable.
     */
    public SetRobotResponse setWebotsSim(boolean webotsSim) {
        if (this.immutable) {
            throw new RuntimeException("webotsSim assigned to an immutable object: " + toString());
        }
        this.webotsSim = webotsSim;
        this.webotsSimDefined = true;
        return this;
    }
    
    /**
     * GET webotsUrl. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getWebotsUrl() {
        if (!this.immutable) {
            throw new RuntimeException("no webotsUrl from an object under construction: " + toString());
        }
        return this.webotsUrl;
    }
    
    /**
     * is the property defined? The property maybe undefined as it is not a required property
     *
     * @return true if the property is defined (has been set)
     */
    public boolean webotsUrlDefined() {
        return this.webotsUrl != null;
    }
    
    /**
     * SET webotsUrl. Object must be mutable.
     */
    public SetRobotResponse setWebotsUrl(String webotsUrl) {
        if (this.immutable) {
            throw new RuntimeException("webotsUrl assigned to an immutable object: " + toString());
        }
        this.webotsUrl = webotsUrl;
        return this;
    }
    
    /**
     * GET connection. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getConnection() {
        if (!this.immutable) {
            throw new RuntimeException("no connection from an object under construction: " + toString());
        }
        return this.connection;
    }
    
    /**
     * SET connection. Object must be mutable.
     */
    public SetRobotResponse setConnection(String connection) {
        if (this.immutable) {
            throw new RuntimeException("connection assigned to an immutable object: " + toString());
        }
        this.connection = connection;
        return this;
    }
    
    /**
     * GET vendor. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getVendor() {
        if (!this.immutable) {
            throw new RuntimeException("no vendor from an object under construction: " + toString());
        }
        return this.vendor;
    }
    
    /**
     * SET vendor. Object must be mutable.
     */
    public SetRobotResponse setVendor(String vendor) {
        if (this.immutable) {
            throw new RuntimeException("vendor assigned to an immutable object: " + toString());
        }
        this.vendor = vendor;
        return this;
    }
    
    /**
     * GET configurationUsed. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getConfigurationUsed() {
        if (!this.immutable) {
            throw new RuntimeException("no configurationUsed from an object under construction: " + toString());
        }
        return this.configurationUsed;
    }
    
    /**
     * SET configurationUsed. Object must be mutable.
     */
    public SetRobotResponse setConfigurationUsed(boolean configurationUsed) {
        if (this.immutable) {
            throw new RuntimeException("configurationUsed assigned to an immutable object: " + toString());
        }
        this.configurationUsed = configurationUsed;
        this.configurationUsedDefined = true;
        return this;
    }
    
    /**
     * GET commandLine. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getCommandLine() {
        if (!this.immutable) {
            throw new RuntimeException("no commandLine from an object under construction: " + toString());
        }
        return this.commandLine;
    }
    
    /**
     * is the property defined? The property maybe undefined as it is not a required property
     *
     * @return true if the property is defined (has been set)
     */
    public boolean commandLineDefined() {
        return this.commandLine != null;
    }
    
    /**
     * SET commandLine. Object must be mutable.
     */
    public SetRobotResponse setCommandLine(String commandLine) {
        if (this.immutable) {
            throw new RuntimeException("commandLine assigned to an immutable object: " + toString());
        }
        this.commandLine = commandLine;
        return this;
    }
    
    /**
     * GET signature. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getSignature() {
        if (!this.immutable) {
            throw new RuntimeException("no signature from an object under construction: " + toString());
        }
        return this.signature;
    }
    
    /**
     * is the property defined? The property maybe undefined as it is not a required property
     *
     * @return true if the property is defined (has been set)
     */
    public boolean signatureDefined() {
        return this.signature != null;
    }
    
    /**
     * SET signature. Object must be mutable.
     */
    public SetRobotResponse setSignature(String signature) {
        if (this.immutable) {
            throw new RuntimeException("signature assigned to an immutable object: " + toString());
        }
        this.signature = signature;
        return this;
    }
    
    /**
     * GET sourceCodeFileExtension. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getSourceCodeFileExtension() {
        if (!this.immutable) {
            throw new RuntimeException("no sourceCodeFileExtension from an object under construction: " + toString());
        }
        return this.sourceCodeFileExtension;
    }
    
    /**
     * SET sourceCodeFileExtension. Object must be mutable.
     */
    public SetRobotResponse setSourceCodeFileExtension(String sourceCodeFileExtension) {
        if (this.immutable) {
            throw new RuntimeException("sourceCodeFileExtension assigned to an immutable object: " + toString());
        }
        this.sourceCodeFileExtension = sourceCodeFileExtension;
        return this;
    }
    
    /**
     * GET binaryFileExtension. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getBinaryFileExtension() {
        if (!this.immutable) {
            throw new RuntimeException("no binaryFileExtension from an object under construction: " + toString());
        }
        return this.binaryFileExtension;
    }
    
    /**
     * SET binaryFileExtension. Object must be mutable.
     */
    public SetRobotResponse setBinaryFileExtension(String binaryFileExtension) {
        if (this.immutable) {
            throw new RuntimeException("binaryFileExtension assigned to an immutable object: " + toString());
        }
        this.binaryFileExtension = binaryFileExtension;
        return this;
    }
    
    /**
     * GET hasWlan. Object must be immutable. Never return null or an undefined/default value.
     */
    public boolean getHasWlan() {
        if (!this.immutable) {
            throw new RuntimeException("no hasWlan from an object under construction: " + toString());
        }
        return this.hasWlan;
    }
    
    /**
     * SET hasWlan. Object must be mutable.
     */
    public SetRobotResponse setHasWlan(boolean hasWlan) {
        if (this.immutable) {
            throw new RuntimeException("hasWlan assigned to an immutable object: " + toString());
        }
        this.hasWlan = hasWlan;
        this.hasWlanDefined = true;
        return this;
    }
    
    /**
     * GET firmwareDefault. Object must be immutable. Never return null or an undefined/default value.
     */
    public String getFirmwareDefault() {
        if (!this.immutable) {
            throw new RuntimeException("no firmwareDefault from an object under construction: " + toString());
        }
        return this.firmwareDefault;
    }
    
    /**
     * is the property defined? The property maybe undefined as it is not a required property
     *
     * @return true if the property is defined (has been set)
     */
    public boolean firmwareDefaultDefined() {
        return this.firmwareDefault != null;
    }
    
    /**
     * SET firmwareDefault. Object must be mutable.
     */
    public SetRobotResponse setFirmwareDefault(String firmwareDefault) {
        if (this.immutable) {
            throw new RuntimeException("firmwareDefault assigned to an immutable object: " + toString());
        }
        this.firmwareDefault = firmwareDefault;
        return this;
    }
    
    /**
     * generates a JSON-object from an immutable bean.<br>
     * Throws a runtime exception if inconsistencies are detected.
     */
    public JSONObject toJson() {
        if (!this.immutable) {
            throw new RuntimeException("no JSON from an object under construction: " + toString());
        }
        JSONObject jsonO = new JSONObject();
        try {
            jsonO.put("_version", "1");
            if (this.cmd != null) {
                jsonO.put("cmd", this.cmd);
            }
            jsonO.put("rc", this.rc);
            if (this.message != null) {
                jsonO.put("message", this.message);
            }
            if (this.cause != null) {
                jsonO.put("cause", this.cause);
            }
            if (this.parameters != null) {
                jsonO.put("parameters", this.parameters);
            }
            jsonO.put("initToken", this.initToken);
            jsonO.put("server.time", this.serverTime);
            jsonO.put("server.version", this.serverVersion);
            if (this.robotWaitDefined) {
                jsonO.put("robot.wait", this.robotWait);
            }
            if (this.robotBattery != null) {
                jsonO.put("robot.battery", this.robotBattery);
            }
            if (this.robotName != null) {
                jsonO.put("robot.name", this.robotName);
            }
            if (this.robotVersion != null) {
                jsonO.put("robot.version", this.robotVersion);
            }
            if (this.robotFirmwareName != null) {
                jsonO.put("robot.firmwareName", this.robotFirmwareName);
            }
            if (this.robotSensorvalues != null) {
                jsonO.put("robot.sensorvalues", this.robotSensorvalues);
            }
            if (this.robotNepoexitvalueDefined) {
                jsonO.put("robot.nepoexitvalue", this.robotNepoexitvalue);
            }
            if (this.robotState != null) {
                jsonO.put("robot.state", this.robotState);
            }
            if (this.notificationsAvailableDefined) {
                jsonO.put("notifications.available", this.notificationsAvailable);
            }
            jsonO.put("robot", this.robot);
            jsonO.put("program", this.program);
            jsonO.put("configuration", this.configuration);
            jsonO.put("sim", this.sim);
            jsonO.put("multipleSim", this.multipleSim);
            jsonO.put("markerSim", this.markerSim);
            jsonO.put("nn", this.nn);
            jsonO.put("availableNNActivationFunctions", this.availableNNActivationFunctions);
            jsonO.put("webotsSim", this.webotsSim);
            if (this.webotsUrl != null) {
                jsonO.put("webotsUrl", this.webotsUrl);
            }
            jsonO.put("connection", this.connection);
            jsonO.put("vendor", this.vendor);
            jsonO.put("configurationUsed", this.configurationUsed);
            if (this.commandLine != null) {
                jsonO.put("commandLine", this.commandLine);
            }
            if (this.signature != null) {
                jsonO.put("signature", this.signature);
            }
            jsonO.put("sourceCodeFileExtension", this.sourceCodeFileExtension);
            jsonO.put("binaryFileExtension", this.binaryFileExtension);
            jsonO.put("hasWlan", this.hasWlan);
            if (this.firmwareDefault != null) {
                jsonO.put("firmwareDefault", this.firmwareDefault);
            }
        } catch (JSONException e) {
            throw new RuntimeException("JSON unparse error when unparsing: " + this, e);
        }
        return jsonO;
    }
    
    @Override
    public String toString() {
        return "SetRobotResponse [immutable=" + this.immutable + ", cmd=" + this.cmd + ", rc=" + this.rc + ", message=" + this.message + ", cause=" + this.cause + ", parameters=" + this.parameters + ", initToken=" + this.initToken + ", serverTime=" + this.serverTime + ", serverVersion=" + this.serverVersion + ", robotWait=" + this.robotWait + ", robotBattery=" + this.robotBattery + ", robotName=" + this.robotName + ", robotVersion=" + this.robotVersion + ", robotFirmwareName=" + this.robotFirmwareName + ", robotSensorvalues=" + this.robotSensorvalues + ", robotNepoexitvalue=" + this.robotNepoexitvalue + ", robotState=" + this.robotState + ", notificationsAvailable=" + this.notificationsAvailable + ", robot=" + this.robot + ", program=" + this.program + ", configuration=" + this.configuration + ", sim=" + this.sim + ", multipleSim=" + this.multipleSim + ", markerSim=" + this.markerSim + ", nn=" + this.nn + ", availableNNActivationFunctions=" + this.availableNNActivationFunctions + ", webotsSim=" + this.webotsSim + ", webotsUrl=" + this.webotsUrl + ", connection=" + this.connection + ", vendor=" + this.vendor + ", configurationUsed=" + this.configurationUsed + ", commandLine=" + this.commandLine + ", signature=" + this.signature + ", sourceCodeFileExtension=" + this.sourceCodeFileExtension + ", binaryFileExtension=" + this.binaryFileExtension + ", hasWlan=" + this.hasWlan + ", firmwareDefault=" + this.firmwareDefault + " ]";
    }
    @Override
    public int hashCode() {
        throw new RuntimeException("no hashCode from transport beans!");
    }
    
    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("no equals from transport beans!");
    }
    
}
