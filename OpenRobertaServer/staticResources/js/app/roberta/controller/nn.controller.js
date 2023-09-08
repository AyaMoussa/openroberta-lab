define(["require", "exports", "guiState.controller", "neuralnetwork.ui", "jquery", "util", "jquery-validate"], function (require, exports, GUISTATE_C, NN_UI, $, UTIL) {
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.reloadViews = exports.mkNNfromNNStepDataAndRunNNEditorForTabExplore = exports.mkNNfromNNStepDataAndRunNNEditor = exports.mkNNfromProgramStartBlock = exports.saveNN2Blockly = exports.programWasReplaced = exports.init = void 0;
    /**
     * initialize the callbacks needed by the NN tab. Called once at front end init time
     */
    function init() {
        $('#tabNN').onWrap('show.bs.tab', function (e) {
            $('#nn').show();
            GUISTATE_C.setView('tabNN');
        }, 'show tabNN');
        $('#tabNN').onWrap('shown.bs.tab', function (e) {
            GUISTATE_C.setProgramSaved(false);
            mkNNfromNNStepDataAndRunNNEditor();
        }, 'shown tabNN');
        $('#tabNN').onWrap('hide.bs.tab', function (e) {
            saveNN2Blockly();
            $('#nn').hide();
        }, 'hide tabNN');
        $('#tabNN').onWrap('hidden.bs.tab', function (e) { }, 'hidden tabNN');
        $('#tabNNexplore').onWrap('show.bs.tab', function (e) {
            $('#nnExplore').show();
            GUISTATE_C.setView('tabNNexplore');
        }, 'show tabNNexplore');
        $('#tabNNexplore').onWrap('shown.bs.tab', function (e) {
            GUISTATE_C.setProgramSaved(false);
            mkNNfromNNStepDataAndRunNNEditorForTabExplore();
        }, 'shown tabNNexplore');
        $('#tabNNexplore').onWrap('hide.bs.tab', function (e) {
            saveNN2Blockly();
            $('#nnExplore').hide();
        }, 'hide tabNNexplore');
        $('#tabNNexplore').onWrap('hidden.bs.tab', function (e) { }, 'hidden tabNNexplore');
        $('#tabNNlearn').onWrap('show.bs.tab', function (e) {
            $('#nnLearn').show();
            GUISTATE_C.setView('tabNNlearn');
        }, 'show tabNNlearn');
        $('#tabNNlearn').onWrap('shown.bs.tab', function (e) {
            GUISTATE_C.setProgramSaved(false);
            mkNNfromNNStepDataAndRunNNEditor();
        }, 'shown tabNNlearn');
        $('#tabNNlearn').onWrap('hide.bs.tab', function (e) {
            saveNN2Blockly();
            $('#nnLearn').hide();
        }, 'hide tabNNlearn');
        $('#tabNNlearn').onWrap('hidden.bs.tab', function (e) { }, 'hidden tabNNlearn');
    }
    exports.init = init;
    /**
     * notify, that a new program was imported into the program tab. In this case -if the simulation tab is open- at simulation close time the NN must
     * not be written back to the blockly XML.
     */
    function programWasReplaced() {
        NN_UI.programWasReplaced();
    }
    exports.programWasReplaced = programWasReplaced;
    /**
     * terminate the nn editor.  Called, when the NN editor or the program terminates. Cleanup:
     * - save the NN to the program XML as data in the start block.
     * - close the edit card
     * - reset node selection (yellow node)
     */
    function saveNN2Blockly(neuralNetwork) {
        if (neuralNetwork === void 0) { neuralNetwork = null; }
        NN_UI.saveNN2Blockly(neuralNetwork);
        NN_UI.resetUiOnTerminate();
    }
    exports.saveNN2Blockly = saveNN2Blockly;
    /**
     * create the NN from the program XML. Called, when the simulation starts
     */
    function mkNNfromProgramStartBlock() {
        var startBlock = UTIL.getTheStartBlock();
        var nnStateAsJson;
        try {
            nnStateAsJson = JSON.parse(startBlock.data);
        }
        catch (e) {
            // nnStateAsJson remains null
        }
        NN_UI.setupNN(nnStateAsJson);
    }
    exports.mkNNfromProgramStartBlock = mkNNfromProgramStartBlock;
    /**
     * create the NN from the program XML and start the NN editor. Called, when the NN tab is opened
     */
    function mkNNfromNNStepDataAndRunNNEditor() {
        mkNNfromProgramStartBlock();
        NN_UI.runNNEditor(GUISTATE_C.hasSim());
    }
    exports.mkNNfromNNStepDataAndRunNNEditor = mkNNfromNNStepDataAndRunNNEditor;
    /**
     * create the NN from the program XML and start the NN editor for tab NN-Explore. Called, when the NN-Explore tab is opened
     */
    function mkNNfromNNStepDataAndRunNNEditorForTabExplore() {
        mkNNfromProgramStartBlock();
        NN_UI.runNNEditorForTabExplore(GUISTATE_C.hasSim());
    }
    exports.mkNNfromNNStepDataAndRunNNEditorForTabExplore = mkNNfromNNStepDataAndRunNNEditorForTabExplore;
    function reloadViews() {
        NN_UI.resetSelections();
        NN_UI.reconstructNNIncludingUI();
        NN_UI.drawNetworkUIForTabExplore();
    }
    exports.reloadViews = reloadViews;
});
