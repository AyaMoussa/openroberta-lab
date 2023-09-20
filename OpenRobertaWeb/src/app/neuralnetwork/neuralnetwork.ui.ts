/**
 * This is an addition to the Open Roberta Lab. It supports self programmed Neural Networks.
 * Our work is heavily based on the tensorflow playground, see https://github.com/tensorflow/playground.
 * The Open Roberta Lab is open source and uses the Apache 2.0 License, see https://www.apache.org/licenses/LICENSE-2.0
 */

import * as H from './neuralnetwork.helper';
import { Link, Network, Node } from './neuralnetwork.nn';
import { State } from './neuralnetwork.uistate';
import * as LOG from 'log';
import { error } from 'log';
import * as MSG from './neuralnetwork.msg';

import * as _D3 from 'd3';
import * as UTIL from 'util';

enum NodeType {
    INPUT,
    HIDDEN,
    OUTPUT,
}

enum FocusStyle {
    CLICK_WEIGHT_BIAS,
    CLICK_NODE,
    SHOW_ALL,
}

enum ExploreType {
    RUN,
    LAYER,
    NEURON,
    STOP,
}

enum TabType {
    DEFINE,
    EXPLORE,
    LEARN,
}

let D3: typeof _D3; // used for lazy loading
type D3Selection = _D3.Selection<any>;

let focusStyle = FocusStyle.SHOW_ALL;
let focusNode = null;

let state: State = null;
let network: Network = null;
let rememberProgramWasReplaced = false;

let heightOfWholeNNDiv = 0;
let widthOfWholeNNDiv = 0;

let inputNeuronNameEditingMode = false;
let hiddenNeuronNameEditingMode = false;
let outputNeuronNameEditingMode = false;
let exploreType: ExploreType = null;

let currentDebugLayer: number = 0;
let [currentDebugNode, currentDebugNodeIndex]: [Node, number] = [null, 0];
let flattenedNetwork: Node[] = null;
let nodesExplored: Node[] = [];
let layersExplored = [];
let isInputSet: boolean = false;
let tabType: TabType = null;
let inputNeuronValueEnteringMode: boolean = false;

export function setupNN(stateFromStartBlock: any) {
    rememberProgramWasReplaced = false;
    state = new State(stateFromStartBlock);
    // wrapper for old NN programs without hiddenNeurons
    if (state.networkShape.length != 0 && state.hiddenNeurons.length == 0) {
        for (let i = 0; i < state.numHiddenLayers; i++) {
            state.hiddenNeurons.push([]);
            for (let j = 0; j < state.networkShape[i]; j++) {
                const id = selectDefaultId(true, i + 1);
                state.hiddenNeurons[i].push(id);
            }
        }
    }
    makeNetworkFromState();
}

export async function runNNEditor(hasSim: boolean) {
    tabType = TabType.DEFINE;
    D3 = await import('d3');
    if (hasSim) {
        D3.select('#goto-sim').style('visibility', 'visible');
        D3.select('#goto-sim').on('click', () => {
            $.when($('#tabProgram').trigger('click')).done(function () {
                $('#simButton').trigger('click');
            });
        });
    } else {
        D3.select('#goto-sim').style('visibility', 'hidden');
    }

    D3.select('#nn-focus').on('change', function () {
        focusStyle = FocusStyle[(this as HTMLSelectElement).value];
        if (focusStyle === undefined || focusStyle === null) {
            focusStyle = FocusStyle.SHOW_ALL;
        }
        if (focusStyle !== FocusStyle.CLICK_NODE) {
            focusNode = null;
        }
        hideAllCards();
        drawNetworkUIForTabDefine();
        $('#nn-explore-focus')
            .val(focusStyle == FocusStyle.CLICK_WEIGHT_BIAS ? 'SHOW_ALL' : (this as HTMLSelectElement).value)
            .change();
        $('#nn-learn-focus')
            .val(focusStyle == FocusStyle.CLICK_WEIGHT_BIAS ? 'SHOW_ALL' : (this as HTMLSelectElement).value)
            .change();
    });

    D3.select('#nn-add-layers').on('click', () => {
        if (state.numHiddenLayers >= 6) {
            return;
        }
        state.networkShape[state.numHiddenLayers] = 2;
        state.hiddenNeurons.push([]);
        for (let i = 0; i < 2; i++) {
            const id = selectDefaultId(true, state.numHiddenLayers + 1);
            state.hiddenNeurons[state.numHiddenLayers].push(id);
        }
        state.numHiddenLayers++;
        hideAllCards();
        reconstructNNIncludingUI();
    });

    D3.select('#nn-remove-layers').on('click', () => {
        if (state.numHiddenLayers <= 0) {
            return;
        }
        state.numHiddenLayers--;
        state.networkShape.splice(state.numHiddenLayers);
        hideAllCards();
        reconstructNNIncludingUI();
    });

    let activationDropdown = D3.select('#nn-activations').on('change', function () {
        state.activationKey = this.value;
        state.activation = H.activations[this.value];
        drawNetworkUIForTabDefine();
    });
    activationDropdown.property('value', getKeyFromValue(H.activations, state.activation));

    D3.select('#nn-show-precision').on('change', function () {
        state.precision = this.value;
        drawNetworkUIForTabDefine();
    });

    D3.select('#nn-get-shape').on('change', updateShapeListener);

    D3.select('#nn-shape-finished-button').on('click', updateShapeListener);

    function updateShapeListener() {
        let val = $('#nn-get-shape')
            .val()
            .toString()
            .trim()
            .split(',')
            .filter((x) => !Number.isNaN(x) && Number(x) !== 0)
            .map((x) => {
                return Number(x) >= 10 ? 9 : Number(x);
            });
        {
            let [previousInputs, previousInputsLength] = [state.inputs, state.inputs.length];
            state.inputs = [];
            val[0] = val[0] >= 9 ? 9 : val[0];
            for (let i = 0; i < val[0]; i++) {
                if (i < previousInputsLength) {
                    state.inputs.push(previousInputs.shift());
                } else {
                    const id = selectDefaultId();
                    state.inputs.push(id);
                }
            }
        }
        {
            state.hiddenNeurons = [];
            state.networkShape = val.slice(1, -1).map((v) => (v >= 9 ? 9 : v));
            state.numHiddenLayers = state.networkShape.length;
            for (let i = 0; i < state.numHiddenLayers; i++) {
                state.hiddenNeurons.push([]);
                for (let j = 0; j < state.networkShape[i]; j++) {
                    const id = selectDefaultId(true, i + 1);
                    state.hiddenNeurons[i].push(id);
                }
            }
        }
        {
            let [previousOutputs, previousOutputsLength] = [state.outputs, state.outputs.length];
            state.outputs = [];
            val[val.length - 1] = val[val.length - 1] >= 9 ? 9 : val[val.length - 1];
            for (let i = 0; i < val[val.length - 1]; i++) {
                if (i < previousOutputsLength) {
                    state.outputs.push(previousOutputs.shift());
                } else {
                    const id = selectDefaultId();
                    state.outputs.push(id);
                }
            }
        }
        hideAllCards();
        reconstructNNIncludingUI();
    }

    // Listen for css-responsive changes and redraw the svg network.
    window.addEventListener('resize', () => {
        hideAllCards();
        drawNetworkUIForTabDefine();
    });
    hideAllCards();
    reconstructNNIncludingUI();
    return;

    function getKeyFromValue(obj: any, value: any): string {
        for (let key in obj) {
            if (obj[key] === value) {
                return key;
            }
        }
        return undefined;
    }
}

export async function runNNEditorForTabExplore(hasSim: boolean) {
    tabType = TabType.EXPLORE;
    D3 = await import('d3');
    if (hasSim) {
        D3.select('#explore-goto-sim').style('visibility', 'visible');
        D3.select('#explore-goto-sim').on('click', () => {
            $.when($('#tabProgram').trigger('click')).done(function () {
                $('#simButton').trigger('click');
            });
        });
    } else {
        D3.select('#explore-goto-sim').style('visibility', 'hidden');
    }

    D3.select('#nn-explore-focus').on('change', function () {
        focusStyle = FocusStyle[(this as HTMLSelectElement).value];
        if (focusStyle === undefined || focusStyle === null) {
            focusStyle = FocusStyle.SHOW_ALL;
        }
        if (focusStyle !== FocusStyle.CLICK_NODE) {
            focusNode = null;
        }
        hideAllCards();
        drawNetworkUIForTabExplore();
        $('#nn-focus')
            .val((this as HTMLSelectElement).value)
            .change();
    });

    D3.select('#nn-explore-run-full').on('click', () => {
        exploreType = ExploreType.RUN;
        network.forwardProp();
        currentDebugLayer = 0;
        currentDebugNodeIndex = 0;
        layersExplored = [];
        nodesExplored = [];
        hideAllCards();
        drawNetworkUIForTabExplore();
    });

    D3.select('#nn-explore-run-layer').on('click', () => {
        exploreType = ExploreType.LAYER;
        isInputSet = false;
        const networkImpl = network.getLayerAndNodeArray();
        if (currentDebugLayer < networkImpl.length) {
            layersExplored.push(...networkImpl[currentDebugLayer++]);
        } else {
            currentDebugLayer = networkImpl.length;
        }
        network.forwardProp();
        currentDebugNodeIndex = 0;
        nodesExplored = [];
        hideAllCards();
        drawNetworkUIForTabExplore();
    });

    D3.select('#nn-explore-run-neuron').on('click', () => {
        exploreType = ExploreType.NEURON;
        isInputSet = false;
        focusNode = null;
        if (flattenedNetwork == null) {
            const networkImpl = network.getLayerAndNodeArray();
            flattenedNetwork = [].concat.apply([], networkImpl);
        }
        if (currentDebugNodeIndex < flattenedNetwork.length) {
            currentDebugNode = flattenedNetwork[currentDebugNodeIndex++];
            nodesExplored.push(currentDebugNode);
        } else {
            currentDebugNodeIndex = flattenedNetwork.length;
        }
        !state.inputs.includes(currentDebugNode.id) && currentDebugNode.updateOutput();
        currentDebugLayer = 0;
        layersExplored = [];
        hideAllCards();
        drawNetworkUIForTabExplore();
    });

    D3.select('#nn-explore-stop').on('click', () => {
        exploreType = ExploreType.STOP;
        resetSelections();
        D3.select('#nn-show-next-neuron').html('');
        hideAllCards();
        drawNetworkUIForTabExplore();
    });

    D3.select('#nn-show-next-neuron').html('');

    // Listen for css-responsive changes and redraw the svg network.
    window.addEventListener('resize', () => {
        hideAllCards();
        drawNetworkUIForTabExplore();
    });

    resetSelections();
    hideAllCards();
    makeNetworkFromState();
    drawNetworkUIForTabExplore();
    return;
}

export async function runNNEditorForTabLearn(hasSim: boolean) {
    tabType = TabType.LEARN;
    D3 = await import('d3');
    if (hasSim) {
        D3.select('#learn-goto-sim').style('visibility', 'visible');
        D3.select('#learn-goto-sim').on('click', () => {
            $.when($('#tabProgram').trigger('click')).done(function () {
                $('#simButton').trigger('click');
            });
        });
    } else {
        D3.select('#learn-goto-sim').style('visibility', 'hidden');
    }

    D3.select('#nn-learn-focus').on('change', function () {
        focusStyle = FocusStyle[(this as HTMLSelectElement).value];
        if (focusStyle === undefined || focusStyle === null) {
            focusStyle = FocusStyle.SHOW_ALL;
        }
        if (focusStyle !== FocusStyle.CLICK_NODE) {
            focusNode = null;
        }
        hideAllCards();
        drawNetworkUIForTabLearn();
        $('#nn-focus')
            .val((this as HTMLSelectElement).value)
            .change();
    });

    D3.select('#nn-learn-run').on('click', () => {
        exploreType = ExploreType.RUN;
        network.forwardProp();
        currentDebugLayer = 0;
        currentDebugNodeIndex = 0;
        hideAllCards();
        drawNetworkUIForTabLearn();
    });

    D3.select('#nn-learn-debug').on('click', () => {
        exploreType = ExploreType.LAYER;
        isInputSet = true;
        const networkImpl = network.getLayerAndNodeArray();
        if (currentDebugLayer < networkImpl.length - 1) {
            currentDebugLayer++;
        } else {
            currentDebugLayer = 1;
        }
        network.forwardProp();
        currentDebugNodeIndex = 0;
        hideAllCards();
        drawNetworkUIForTabLearn();
    });

    D3.select('#nn-learn-stop').on('click', () => {
        exploreType = ExploreType.STOP;
        isInputSet = false;
        hideAllCards();
        drawNetworkUIForTabLearn();
    });

    D3.select('#nn-get-learning-rate').on('change', updateLearningRateListener);

    D3.select('#nn-learning-rate-finished-button').on('click', updateLearningRateListener);

    D3.select('#nn-get-regularization-rate').on('change', updateRegularizationRateListener);

    D3.select('#nn-regularization-rate-finished-button').on('click', updateRegularizationRateListener);

    function updateLearningRateListener() {}

    function updateRegularizationRateListener() {}

    // Listen for css-responsive changes and redraw the svg network.
    window.addEventListener('resize', () => {
        hideAllCards();
        drawNetworkUIForTabLearn();
    });

    resetSelections();
    hideAllCards();
    makeNetworkFromState();
    drawNetworkUIForTabLearn();
    return;
}

export function resetUiOnTerminate() {
    hideAllCards();
    focusNode = null;
}

export function reconstructNNIncludingUI() {
    makeNetworkFromState();
    drawNetworkUIForTabDefine();
}

export function drawNetworkUIForTabExplore() {
    $('#nn-explore-focus-label').text(MSG.get('NN_EXPLORE_FOCUS_OPTION'));
    $('#nn-explore-focus [value="CLICK_NODE"]').text(MSG.get('NN_EXPLORE_CLICK_NODE'));
    $('#nn-explore-focus [value="SHOW_ALL"]').text(MSG.get('NN_EXPLORE_SHOW_ALL'));
    $('#nn-show-math-label').text(MSG.get('NN_SHOW_MATH'));
    $('#nn-show-next-neuron-label').text(MSG.get('NN_EXPLORE_SHOW_NEXT_NEURON'));

    drawTheNetwork();
}

export function drawNetworkUIForTabLearn() {
    $('#nn-learn-focus-label').text(MSG.get('NN_EXPLORE_FOCUS_OPTION'));
    $('#nn-learn-focus [value="CLICK_NODE"]').text(MSG.get('NN_EXPLORE_CLICK_NODE'));
    $('#nn-learn-focus [value="SHOW_ALL"]').text(MSG.get('NN_EXPLORE_SHOW_ALL'));
    $('#nn-get-regularization-rate-label').text(MSG.get('regularization rate'));
    $('#nn-get-learning-rate-label').text(MSG.get('learning rate'));

    drawTheNetwork();
}

function drawNetworkUIForTabDefine(): void {
    $('#nn-activation-label').text(MSG.get('NN_ACTIVATION'));
    $('#nn-regularization-label').text(MSG.get('NN_REGULARIZATION'));
    $('#nn-focus-label').text(MSG.get('NN_FOCUS_OPTION'));
    $('#nn-focus [value="CLICK_WEIGHT_BIAS"]').text(MSG.get('NN_CLICK_WEIGHT_BIAS'));
    $('#nn-focus [value="CLICK_NODE"]').text(MSG.get('NN_CLICK_NODE'));
    $('#nn-focus [value="SHOW_ALL"]').text(MSG.get('NN_SHOW_ALL'));
    $('#nn-get-shape-label').text(MSG.get('NN_SHAPE'));
    $('#nn-show-precision-label').text(MSG.get('NN_SHOW_PRECISION'));

    let layerKey = state.numHiddenLayers === 1 ? 'NN_HIDDEN_LAYER' : 'NN_HIDDEN_LAYERS';
    $('#layers-label').text(MSG.get(layerKey));
    $('#num-layers').text(state.numHiddenLayers);

    drawTheNetwork();
}

function drawTheNetwork() {
    let tabSuffix: string = '';
    let tabCallback: Function = null;

    switch (tabType) {
        case TabType.DEFINE:
            tabSuffix = '';
            tabCallback = runNameCard;
            break;
        case TabType.EXPLORE:
            tabSuffix = '-explore';
            tabCallback = runValueCard;
            break;
        case TabType.LEARN:
            tabSuffix = '-learn';
            // tabCallback = runValueCard;
            break;
        default:
            error(`Invalid tab encountered ${tabType}`);
    }

    const networkImpl = network.getLayerAndNodeArray();
    const svg: D3Selection = D3.select(`#nn${tabSuffix}-svg`);

    svg.select('g.core').remove();
    D3.select(`#nn${tabSuffix}-main-part`).selectAll('div.canvas').remove();
    D3.select(`#nn${tabSuffix}-main-part`).selectAll('div.nn-plus-minus-neurons').remove();

    const nnD3 = D3.select(`#nn${tabSuffix}`)[0][0] as HTMLDivElement;
    const topControlD3 = D3.select(`#nn${tabSuffix}-top-controls`)[0][0] as HTMLDivElement;
    const mainPartHeight = nnD3.clientHeight - topControlD3.clientHeight + (tabType == TabType.DEFINE ? -50 : -75);

    // set the width of the svg container.
    const mainPart = D3.select(`#nn${tabSuffix}-main-part`)[0][0] as HTMLDivElement;
    mainPart.setAttribute('style', 'height:' + mainPartHeight + 'px');

    widthOfWholeNNDiv = mainPart.clientWidth;
    heightOfWholeNNDiv = mainPartHeight;

    svg.attr('width', widthOfWholeNNDiv);
    svg.attr('height', heightOfWholeNNDiv);

    const numLayers = networkImpl.length;

    // vertical distance (Y) between nodes and node size
    let maxNumberOfNodesOfAllLayers = networkImpl.map((layer) => layer.length).reduce((a, b) => Math.max(a, b), 0);
    maxNumberOfNodesOfAllLayers = maxNumberOfNodesOfAllLayers < 1 ? 1 : maxNumberOfNodesOfAllLayers;
    const totalYBetweenTwoNodes = heightOfWholeNNDiv / maxNumberOfNodesOfAllLayers;
    const nodeSize = (totalYBetweenTwoNodes < 100 ? totalYBetweenTwoNodes : 100) / 2;
    const usedYBetweenTwoNodes = (heightOfWholeNNDiv - 2 * nodeSize) / maxNumberOfNodesOfAllLayers;
    const biasSize = 10;

    // horizontal distance (X) between layers
    const maxXBetweenTwoLayers = (widthOfWholeNNDiv - numLayers * nodeSize) / (numLayers - 1);
    const usedXBetweenTwoLayers = maxXBetweenTwoLayers > 500 ? 500 : maxXBetweenTwoLayers;
    const startXFirstLayer = (widthOfWholeNNDiv - usedXBetweenTwoLayers * (numLayers - 1)) / 2;
    function nodeStartY(nodeIndex: number): number {
        return nodeIndex * usedYBetweenTwoNodes + nodeSize / 2;
    }
    function layerStartX(layerIdx: number): number {
        return startXFirstLayer + layerIdx * usedXBetweenTwoLayers - nodeSize / 2;
    }

    // Map of all node and link coordinates.
    let node2coord: { [id: string]: { cx: number; cy: number } } = {};
    let container: D3Selection = svg
        .append('g')
        .classed('core', true)
        .attr('transform', tabType == TabType.DEFINE ? `translate(3,3)` : `translate(3,50)`);

    // Draw the input layer separately.
    let numNodes = networkImpl[0].length;
    let cxI = layerStartX(0);
    addPlusMinusControl(cxI - nodeSize / 2 - biasSize, 0);
    for (let i = 0; i < numNodes; i++) {
        let node = networkImpl[0][i];
        let cy = nodeStartY(i);
        node2coord[node.id] = { cx: cxI, cy: cy };
        drawNode(node, NodeType.INPUT, cxI, cy, container);
    }
    // Draw the intermediate layers, exclude input (id:0) and output (id:numLayers-1)
    for (let layerIdx = 1; layerIdx < numLayers - 1; layerIdx++) {
        let numNodes = networkImpl[layerIdx].length;
        let cxH = layerStartX(layerIdx);
        addPlusMinusControl(cxH - nodeSize / 2 - biasSize, layerIdx);
        for (let i = 0; i < numNodes; i++) {
            let node = networkImpl[layerIdx][i];
            let cy = nodeStartY(i);
            node2coord[node.id] = { cx: cxH, cy: cy };
            drawNode(node, NodeType.HIDDEN, cxH, cy, container);
            // Draw links.
            for (let j = 0; j < node.inputLinks.length; j++) {
                let link = node.inputLinks[j];
                if ((tabType == TabType.EXPLORE && link.weight.get() != 0) || tabType == TabType.DEFINE || tabType == TabType.LEARN) {
                    drawLink(link, node2coord, networkImpl, container, j === 0, j, node.inputLinks.length);
                }
            }
        }
    }

    // Draw the output nodes separately.
    {
        let outputLayer = networkImpl[numLayers - 1];
        let numOutputs = outputLayer.length;
        let cxO = layerStartX(numLayers - 1);
        addPlusMinusControl(cxO - nodeSize / 2 - biasSize, numLayers - 1);
        for (let j = 0; j < numOutputs; j++) {
            let node = outputLayer[j];
            let cy = nodeStartY(j);
            node2coord[node.id] = { cx: cxO, cy: cy };
            drawNode(node, NodeType.OUTPUT, cxO, cy, container);
            // Draw links.
            for (let i = 0; i < node.inputLinks.length; i++) {
                let link = node.inputLinks[i];
                if ((tabType == TabType.EXPLORE && link.weight.get() != 0) || tabType == TabType.DEFINE || tabType == TabType.LEARN) {
                    drawLink(link, node2coord, networkImpl, container, j === 0, j, node.inputLinks.length);
                }
            }
        }
    }

    // Adjust the height of the features column.
    let height = getRelativeHeight(D3.select(`#nn${tabSuffix}-network`));
    D3.select(`.nn${tabSuffix}-features`).style('height', height + 'px');

    updateUI(tabSuffix);
    return;

    function drawNode(node: Node, nodeType: NodeType, cx: number, cy: number, container: D3Selection) {
        if (node.id === '') {
        }

        let nodeId = node.id;
        let x = cx - nodeSize / 2;
        let y = cy - nodeSize / 2;
        let nodeClass = nodeType === NodeType.INPUT ? 'node_input' : nodeType === NodeType.HIDDEN ? 'node_hidden' : 'node_output';
        let nodeGroup: D3Selection = container.append('g').attr({
            class: nodeClass,
            id: `${nodeId}`,
            transform: `translate(${x},${y})`,
        });

        let mainRectAngle = nodeGroup.append('rect').attr({
            x: 0,
            y: 0,
            width: nodeSize,
            height: nodeSize,
            'marker-start': 'url(#markerArrow)',
        });
        if (focusNode !== undefined && focusNode != null && focusNode.id === node.id) {
            mainRectAngle.attr('style', 'outline: medium solid #fbdc00;');
        }
        let theWholeNNSvgNode = D3.select(`#nn${tabSuffix}-svg`).node();
        nodeGroup
            .on('dblclick', function () {
                // works well in Chrome, not in Firefox...
                tabCallback && tabCallback(node, D3.mouse(theWholeNNSvgNode));
            })
            .on('click', function () {
                if ((D3.event as any).shiftKey) {
                    tabCallback && tabCallback(node, D3.mouse(theWholeNNSvgNode));
                } else if (inputNeuronNameEditingMode && tabType === TabType.DEFINE && nodeType == NodeType.INPUT) {
                    tabCallback && tabCallback(node, D3.mouse(theWholeNNSvgNode));
                } else if (hiddenNeuronNameEditingMode && nodeType == NodeType.HIDDEN) {
                    tabCallback && tabCallback(node, D3.mouse(theWholeNNSvgNode));
                } else if (outputNeuronNameEditingMode && nodeType == NodeType.OUTPUT) {
                    tabCallback && tabCallback(node, D3.mouse(theWholeNNSvgNode));
                } else if (inputNeuronValueEnteringMode && tabType === TabType.EXPLORE && nodeType == NodeType.INPUT) {
                    tabCallback && tabCallback(node, D3.mouse(theWholeNNSvgNode));
                } else {
                    if (focusNode == node) {
                        focusNode = null;
                    } else {
                        focusNode = node;
                    }
                    drawTheNetwork();
                }
            });

        let labelForId = nodeGroup.append('text').attr({
            class: 'main-label',
            x: 10,
            y: 0.66 * nodeSize,
            'text-anchor': 'start',
            cursor: 'default',
        });
        labelForId.append('tspan').text(nodeId);
        if (nodeType !== NodeType.INPUT) {
            drawBias(container, nodeGroup, node);
        }
        if (tabType == TabType.EXPLORE) {
            if (isInputSet && nodeType == NodeType.INPUT) {
                drawNodeOutput(container, nodeGroup, node, NodeType.INPUT);
            }
            if (exploreType == ExploreType.RUN) {
                drawNodeOutput(container, nodeGroup, node, nodeType);
            }
            if (exploreType == ExploreType.LAYER) {
                if (layersExplored.includes(node)) {
                    drawNodeOutput(container, nodeGroup, node, nodeType);
                }
            }
            if (exploreType == ExploreType.NEURON) {
                if (nodesExplored.includes(node)) {
                    drawNodeOutput(container, nodeGroup, node, nodeType);
                }
                D3.select('#nn-show-next-neuron').html(
                    currentDebugNode.id == flattenedNetwork[flattenedNetwork.length - 1].id ? '' : flattenedNetwork[currentDebugNodeIndex].id
                );
            } else {
                D3.select('#nn-show-next-neuron').html('');
            }
        }
        // Draw the node's canvas.
        D3.select(`#nn${tabSuffix}-network`)
            .insert('div', ':first-child')
            .attr({
                id: `canvas-${nodeId}`,
                class: 'canvas',
            })
            .style({
                position: 'absolute',
                left: `${x + 3}px`,
                top: `${y + 3}px`,
            });
    }

    let valShiftToRight = true;

    function drawLink(
        link: Link,
        node2coord: { [id: string]: { cx: number; cy: number } },
        network: Node[][],
        container: D3Selection,
        isFirst: boolean,
        index: number,
        length: number
    ) {
        let line = container.insert('path', ':first-child');
        let source = node2coord[link.source.id];
        let dest = node2coord[link.dest.id];
        let datum = {
            source: {
                y: source.cx + nodeSize / 2 + 2,
                x: source.cy,
            },
            target: {
                y: dest.cx - nodeSize / 2,
                x: dest.cy + ((index - (length - 1) / 2) / length) * 12,
            },
        };
        let diagonal = D3.svg.diagonal().projection((d) => [d.y, d.x]);
        line.attr({
            'marker-start': 'url(#markerArrow)',
            class: 'link',
            id: link.source.id + '-' + link.dest.id,
            d: diagonal(datum, 0),
        });

        // Show the value of the link depending on focus-style
        if (focusStyle === FocusStyle.SHOW_ALL || (focusStyle === FocusStyle.CLICK_NODE && (link.source === focusNode || link.dest === focusNode))) {
            let lineNode = line.node() as any;
            valShiftToRight = !valShiftToRight;
            let posVal = focusStyle === FocusStyle.SHOW_ALL ? (valShiftToRight ? 0.6 : 0.4) : link.source === focusNode ? 0.6 : 0.4;
            let pointForWeight = lineNode.getPointAtLength(lineNode.getTotalLength() * posVal);
            drawValue(
                container,
                link.source.id + '-' + link.dest.id + `${tabSuffix}`,
                pointForWeight.x,
                pointForWeight.y - 10,
                link.weight.get(),
                link.weight.getWithPrecision(state.precision, state.weightSuppressMultOp)
            );
        }

        // Add an invisible thick path that will be used for editing the weight value on click.
        const pathIfClickFocus = focusStyle === FocusStyle.CLICK_NODE && (link.source === focusNode || link.dest === focusNode);
        const pathOtherFoci = focusStyle === FocusStyle.SHOW_ALL || focusStyle === FocusStyle.CLICK_WEIGHT_BIAS;
        if (pathIfClickFocus || pathOtherFoci) {
            let cssForPath = focusStyle !== FocusStyle.CLICK_NODE ? 'nn-weight-click' : 'nn-weight-show-click';
            container
                .append('path')
                .attr('d', diagonal(datum, 0))
                .attr('class', cssForPath)
                .on('click', function () {
                    tabType == TabType.DEFINE && runEditCard(link, D3.mouse(this));
                });
        }
        return line;
    }

    function getRelativeHeight(selection) {
        let node = selection.node() as HTMLAnchorElement;
        return node.offsetHeight + node.offsetTop;
    }

    function drawBias(container: D3Selection, nodeGroup: D3Selection, node: Node) {
        const nodeId = node.id;
        if (focusStyle === FocusStyle.SHOW_ALL || (focusStyle === FocusStyle.CLICK_NODE && focusNode === node)) {
            let biasRect = drawValue(
                nodeGroup,
                nodeId + `${tabSuffix}`,
                -biasSize - 2,
                nodeSize + 2 * biasSize,
                node.bias.get(),
                node.bias.getWithPrecision(state.precision, state.weightSuppressMultOp)
            );
            biasRect.attr('class', 'nn-bias-click');
            if (focusStyle !== FocusStyle.CLICK_NODE || focusNode === node) {
                biasRect.on('click', function () {
                    (D3.event as any).stopPropagation();
                    tabType == TabType.DEFINE && runEditCard(node, D3.mouse(container.node()));
                });
            }
        } else {
            let biasRect = nodeGroup.append('rect').attr({
                id: `bias-${nodeId}`,
                x: -biasSize - 2,
                y: nodeSize - biasSize + 3,
                width: biasSize,
                height: biasSize,
            });
            biasRect.attr('class', 'nn-bias-click');
            if (focusStyle !== FocusStyle.CLICK_NODE || focusNode === node) {
                biasRect.on('click', function () {
                    (D3.event as any).stopPropagation();
                    tabType == TabType.DEFINE && runEditCard(node, D3.mouse(container.node()));
                });
            }
        }
    }

    function drawNodeOutput(container: D3Selection, nodeGroup: D3Selection, node: Node, nodeType: NodeType): void {
        let nodeOutputForUI: string = '';
        if (nodeType == NodeType.INPUT) {
            nodeOutputForUI =
                node.outputForUI.endsWith(',') || node.outputForUI.endsWith('.') ? node.outputForUI.slice(0, node.outputForUI.length - 1) : node.outputForUI;
        } else {
            nodeOutputForUI = node.output.toString();
        }
        drawValue(nodeGroup, `out-${node.id}`, 4.5 * biasSize, nodeSize - 4.5 * biasSize, node.output, nodeOutputForUI, true);
    }

    function drawValue(
        container: D3Selection,
        id: string,
        x: number,
        y: number,
        valueForColor: number,
        valueToShow: string,
        forNodeOutput: boolean = false
    ): D3Selection {
        container.append('rect').attr('id', 'rect-val-' + id);
        const text = container
            .append('text')
            .attr({
                class: 'nn-showval',
                id: 'val-' + id,
                x: x,
                y: y,
            })
            .text(valueToShow);
        drawValuesBox(text, valueForColor, forNodeOutput);
        return text;
    }

    function addPlusMinusControl(x: number, layerIdx: number) {
        if (tabType == TabType.DEFINE) {
            let div = D3.select('#nn-network').append('div').classed('nn-plus-minus-neurons', true).style('left', `${x}px`);
            let isInputLayer = layerIdx == 0;
            let isOutputLayer = layerIdx == numLayers - 1;
            let hiddenIdx = layerIdx - 1;
            let firstRow = div.append('div');
            let callbackPlus = null;
            if (isInputLayer) {
                callbackPlus = () => {
                    let numNeurons = state.inputs.length;
                    if (numNeurons >= 9) {
                        return;
                    }
                    const id = selectDefaultId();
                    state.inputs.push(id);
                    hideAllCards();
                    reconstructNNIncludingUI();
                };
            } else if (isOutputLayer) {
                callbackPlus = () => {
                    let numNeurons = state.outputs.length;
                    if (numNeurons >= 9) {
                        return;
                    }
                    const id = selectDefaultId();
                    state.outputs.push(id);
                    hideAllCards();
                    reconstructNNIncludingUI();
                };
            } else {
                callbackPlus = () => {
                    let numNeurons = state.networkShape[hiddenIdx];
                    if (numNeurons >= 9) {
                        return;
                    }
                    const id = selectDefaultId(true, hiddenIdx + 1);
                    state.hiddenNeurons[hiddenIdx].push(id);
                    state.networkShape[hiddenIdx]++;
                    hideAllCards();
                    reconstructNNIncludingUI();
                };
            }
            firstRow
                .append('button')
                .attr('class', 'nn-btn nn-plus-minus-neuron-button')
                .on('click', callbackPlus)
                .append('span')
                .attr('class', 'typcn typcn-plus');

            let callbackMinus = null;
            if (isInputLayer) {
                callbackMinus = () => {
                    let numNeurons = state.inputs.length;
                    if (numNeurons <= 1) {
                        return;
                    }
                    state.inputs.pop();
                    hideAllCards();
                    reconstructNNIncludingUI();
                };
            } else if (isOutputLayer) {
                callbackMinus = () => {
                    let numNeurons = state.outputs.length;
                    if (numNeurons <= 1) {
                        return;
                    }
                    state.outputs.pop();
                    hideAllCards();
                    reconstructNNIncludingUI();
                };
            } else {
                callbackMinus = () => {
                    let numNeurons = state.networkShape[hiddenIdx];
                    if (numNeurons <= 1) {
                        return;
                    }
                    state.networkShape[hiddenIdx]--;
                    hideAllCards();
                    reconstructNNIncludingUI();
                };
            }
            firstRow
                .append('button')
                .attr('class', 'nn-btn nn-plus-minus-neuron-button')
                .on('click', callbackMinus)
                .append('span')
                .attr('class', 'typcn typcn-minus');

            let shapeToShow = [state.inputs.length, ...state.networkShape, state.outputs.length];
            $('#nn-get-shape').val(`${shapeToShow.toString()}`);

            if (isInputLayer) {
                let button = firstRow.append('button');
                button
                    .attr('class', 'nn-btn nn-plus-minus-neuron-button')
                    .on('click', () => {
                        inputNeuronNameEditingMode = !inputNeuronNameEditingMode;
                        if (inputNeuronNameEditingMode) {
                            D3.event['target'].parentElement.classList.add('active-input');
                            D3.event['target'].classList.add('active-input');
                        } else {
                            D3.event['target'].parentElement.classList.remove('active-input');
                            D3.event['target'].classList.remove('active-input');
                        }
                    })
                    .append('span')
                    .attr('class', 'typcn typcn-edit');
                if (inputNeuronNameEditingMode) {
                    button.classed('active-input', true);
                } else {
                    button.classed('active-input', false);
                }
            } else if (isOutputLayer) {
                let button = firstRow.append('button');
                button
                    .attr('class', 'nn-btn nn-plus-minus-neuron-button')
                    .on('click', () => {
                        outputNeuronNameEditingMode = !outputNeuronNameEditingMode;
                        if (outputNeuronNameEditingMode) {
                            D3.event['target'].parentElement.classList.add('active-output');
                            D3.event['target'].classList.add('active-output');
                        } else {
                            D3.event['target'].parentElement.classList.remove('active-output');
                            D3.event['target'].classList.remove('active-output');
                        }
                    })
                    .append('span')
                    .attr('class', 'typcn typcn-edit');
                if (outputNeuronNameEditingMode) {
                    button.classed('active-output', true);
                } else {
                    button.classed('active-output', false);
                }
            } else {
                let button = firstRow.append('button');
                button
                    .attr('class', 'nn-btn nn-plus-minus-neuron-button')
                    .on('click', () => {
                        hiddenNeuronNameEditingMode = !hiddenNeuronNameEditingMode;
                        if (hiddenNeuronNameEditingMode) {
                            D3.event['target'].parentElement.classList.add('active-hidden');
                            D3.event['target'].classList.add('active-hidden');
                        } else {
                            D3.event['target'].parentElement.classList.remove('active-hidden');
                            D3.event['target'].classList.remove('active-hidden');
                        }
                    })
                    .append('span')
                    .attr('class', 'typcn typcn-edit');
                if (hiddenNeuronNameEditingMode) {
                    button.classed('active-hidden', true);
                } else {
                    button.classed('active-hidden', false);
                }
            }
        } else if (tabType == TabType.EXPLORE) {
            if (layerIdx == 0) {
                let div = D3.select('#nn-explore-network')
                    .append('div')
                    .classed('nn-plus-minus-neurons', true)
                    .style('left', `${x + 20}px`);
                let firstRow = div.append('div');
                let button = firstRow.append('button');
                button
                    .attr('class', 'nn-btn nn-input-value-button')
                    .on('click', () => {
                        inputNeuronValueEnteringMode = !inputNeuronValueEnteringMode;
                        if (inputNeuronValueEnteringMode) {
                            D3.event['target'].parentElement.classList.add('active-input');
                            D3.event['target'].classList.add('active-input');
                        } else {
                            D3.event['target'].parentElement.classList.remove('active-input');
                            D3.event['target'].classList.remove('active-input');
                        }
                    })
                    .append('span')
                    .attr('class', 'typcn typcn-edit');
                if (inputNeuronValueEnteringMode) {
                    button.classed('active-input', true);
                } else {
                    button.classed('active-input', false);
                }
            }
        }
    }
}

function runEditCard(nodeOrLink: Node | Link, coordinates: [number, number]) {
    let editCard = D3.select('#nn-editCard');
    let plusButton = D3.select('#nn-type-plus');
    let minusButton = D3.select('#nn-type-minus');
    let finishedButton = D3.select('#nn-type-finished');
    let cancelButton = D3.select('#nn-type-cancel');

    let input = editCard.select('input');
    input.property('value', nodeOrLink2Value(nodeOrLink));
    let oldValue = nodeOrLink2Value(nodeOrLink);

    hideAllCards();
    input
        .on('keydown', () => {
            let event = D3.event as any;
            if (event.key === 'h' || event.key === 'i') {
                event.target.value = H.updValue(event.target.value, 1);
                event.preventDefault && event.preventDefault();
                value2NodeOrLink(nodeOrLink, event.target.value);
            } else if (event.key === 'r' || event.key === 'd') {
                event.target.value = H.updValue(event.target.value, -1);
                event.preventDefault && event.preventDefault();
                value2NodeOrLink(nodeOrLink, event.target.value);
            } else if (event.which === 13) {
                hideAllCards();
                event.preventDefault && event.preventDefault();
                return false;
            } else if (event.which === 27) {
                hideAllCards();
                event.preventDefault && event.preventDefault();
                value2NodeOrLink(nodeOrLink, oldValue);
            }
            (input.node() as HTMLInputElement).focus();
        })
        .on('input', () => {
            let event = D3.event as any;
            value2NodeOrLink(nodeOrLink, event.target.value);
        });
    plusButton.on('click', () => {
        let oldV = input.property('value');
        let newV = H.updValue(oldV, 1);
        input.property('value', newV);
        value2NodeOrLink(nodeOrLink, newV);
        return;
    });
    minusButton.on('click', () => {
        let oldV = input.property('value');
        let newV = H.updValue(oldV, -1);
        input.property('value', newV);
        value2NodeOrLink(nodeOrLink, newV);
        return;
    });
    finishedButton.on('click', () => {
        hideAllCards();
        return false;
    });
    cancelButton.on('click', () => {
        hideAllCards();
        value2NodeOrLink(nodeOrLink, oldValue);
    });
    let xPos = coordinates[0] + 20;
    let yPos = coordinates[1];
    if (xPos > widthOfWholeNNDiv - 360) {
        xPos = widthOfWholeNNDiv - 370;
    }
    // abandoned idea for tablets. Better use a floating keyboard.
    // let yPos = coordinates[1];
    // if (yPos > heightOfWholeNNDiv / 2.0) {
    //    yPos = yPos / 2.0;
    // }
    editCard.style({
        left: `${xPos}px`,
        top: `${yPos}px`,
        display: 'block',
    });
    let name = nodeOrLink instanceof Link ? 'NN_WEIGHT' : 'NN_BIAS';
    editCard.select('.nn-type').text(MSG.get(name));
    (input.node() as HTMLInputElement).select();
}

function checkNeuronNameIsValid(oldName: string, newName: string): string {
    const validIdRegexp = new RegExp('^[A-Za-z][A-Za-z0-9_]*$');
    if (!validIdRegexp.test(newName)) {
        return 'NN_INVALID_NEURONNAME';
    }
    if (oldName === newName) {
        return null;
    }
    let allNodes = network.network;
    if (allNodes.find((layer) => layer.find((neuron) => neuron.id === newName))) {
        return 'NN_USED_NEURONNAME';
    }
    return null;
}

function checkUserInputIsNumber(input: string): boolean {
    const validInputRegexp = new RegExp('^-?\\d*[.]?\\d*$');
    return validInputRegexp.test(input.trim());
}

function updateNodeName(node: Node, newId: string) {
    let oldId = node.id;
    for (let i = 0; i < state.inputs.length; i++) {
        if (state.inputs[i] === node.id) {
            state.inputs[i] = newId;
        }
    }
    for (let i = 0; i < state.hiddenNeurons.length; i++) {
        for (let j = 0; j < state.hiddenNeurons[i].length; j++) {
            if (state.hiddenNeurons[i][j] === node.id) {
                state.hiddenNeurons[i][j] = newId;
            }
        }
    }
    for (let i = 0; i < state.outputs.length; i++) {
        if (state.outputs[i] === node.id) {
            state.outputs[i] = newId;
        }
    }
    node.id = newId;
    UTIL.renameNeuron(oldId, newId);
}

function hideAllCards() {
    if (D3 !== undefined && D3 !== null) {
        let editCard = D3.select('#nn-editCard');
        editCard.style('display', 'none');
        let nameCard = D3.select('#nn-nameCard');
        nameCard.style('display', 'none');
        let valueCard = D3.select('#nn-valueCard');
        valueCard.style('display', 'none');
    }
}

export function resetSelections(): void {
    exploreType = null;
    currentDebugLayer = 0;
    [currentDebugNode, currentDebugNodeIndex] = [null, 0];
    flattenedNetwork = null;
    nodesExplored = [];
    layersExplored = [];
    isInputSet = false;
}

function runNameCard(node: Node, coordinates: [number, number]) {
    let nameCard = D3.select('#nn-nameCard');
    let finishedButton = D3.select('#nn-name-finished');
    let cancelButton = D3.select('#nn-name-cancel');
    let input = nameCard.select('input');
    input.property('value', node.id);

    let message = D3.select('#nn-name-message');
    message.style('color', '#333');
    message.text(MSG.get('NN_CHANGE_NEURONNAME'));

    hideAllCards();
    function inputValueEventListener() {
        let userInput = input.property('value');
        let check = checkNeuronNameIsValid(node.id, userInput);
        if (check === null) {
            updateNodeName(node, userInput);
            hideAllCards();
            drawNetworkUIForTabDefine();
        } else {
            message.style('color', 'red');
            message.text(MSG.get(check));
        }
    }

    input.on('keydown', () => {
        let event = D3.event as any;
        if (event.which === 13) {
            inputValueEventListener();
        } else if (event.which === 27) {
            hideAllCards();
        }
    });
    finishedButton.on('click', () => {
        let event = D3.event as any;
        event.preventDefault && event.preventDefault();
        inputValueEventListener();
    });
    cancelButton.on('click', () => {
        let event = D3.event as any;
        event.preventDefault && event.preventDefault();
        hideAllCards();
    });

    let xPos = coordinates[0] + 20;
    let yPos = coordinates[1];
    if (xPos > widthOfWholeNNDiv - 320) {
        xPos = widthOfWholeNNDiv - 330;
    }
    nameCard.style({
        left: `${xPos}px`,
        top: `${yPos}px`,
        display: 'block',
    });
    let name = 'POPUP_NAME';
    nameCard.select('.nn-type').text(MSG.get(name));
    (input.node() as HTMLInputElement).select();
}

function runValueCard(node: Node, coordinates: [number, number]) {
    if (node.inputLinks.length !== 0) {
        return; // only input neurons can take input values for forward prop step
    }
    let valueCard = D3.select('#nn-valueCard');
    let plusButton = D3.select('#nn-value-plus');
    let minusButton = D3.select('#nn-value-minus');
    let finishedButton = D3.select('#nn-value-finished');
    let cancelButton = D3.select('#nn-value-cancel');
    let input = valueCard.select('input');
    input.property('value', node.outputForUI);

    let message = D3.select('#nn-value-message');
    message.style('color', '#333');
    message.text(MSG.get('NN_CHANGE_INPUT_NEURON_VALUE'));

    function inputValueEventListener() {
        let userInput = input.property('value');
        let check = checkUserInputIsNumber(userInput.replace(',', '.'));
        if (check) {
            network.setInputNeuronVal(node.id, Number(userInput.replace(',', '.')));
            network.setInputNeuronValForUI(node.id, userInput);
            resetSelections();
            isInputSet = true;
            hideAllCards();
            drawNetworkUIForTabExplore();
        } else {
            message.style('color', 'red');
            message.text(MSG.get('NN_INVALID_INPUT_NEURON_VALUE'));
        }
    }

    input.on('keydown', () => {
        let event = D3.event as any;
        if (event.key === 'h' || event.key === 'i') {
            event.target.value = H.updValue(event.target.value, 1);
            event.preventDefault && event.preventDefault();
        } else if (event.key === 'r' || event.key === 'd') {
            event.target.value = H.updValue(event.target.value, -1);
            event.preventDefault && event.preventDefault();
        } else if (event.which === 13) {
            inputValueEventListener();
        } else if (event.which === 27) {
            hideAllCards();
        }
    });
    plusButton.on('click', () => {
        let oldV = input.property('value');
        let newV = H.updValue(oldV, 1);
        input.property('value', newV);
        return;
    });
    minusButton.on('click', () => {
        let oldV = input.property('value');
        let newV = H.updValue(oldV, -1);
        input.property('value', newV);
        return;
    });
    finishedButton.on('click', () => {
        let event = D3.event as any;
        event.preventDefault && event.preventDefault();
        inputValueEventListener();
    });
    cancelButton.on('click', () => {
        let event = D3.event as any;
        event.preventDefault && event.preventDefault();
        hideAllCards();
    });

    let xPos = coordinates[0] + 20;
    let yPos = coordinates[1];
    if (xPos > widthOfWholeNNDiv - 320) {
        xPos = widthOfWholeNNDiv - 330;
    }
    valueCard.style({
        left: `${xPos}px`,
        top: `${yPos}px`,
        display: 'block',
    });
    valueCard.select('.nn-type').text(MSG.get('POPUP_VALUE'));
    (input.node() as HTMLInputElement).select();
}

function updateUI(tabSuffix: string) {
    const svgId = `#nn${tabSuffix}-svg`;
    const container = D3.select(svgId).select('g.core');
    updateLinksUI(container);
    updateNodesUI();

    function updateLinksUI(container) {
        let linkWidthScale = mkWidthScale();
        let colorScale = mkColorScaleWeight();
        network.forEachLink((link) => {
            const baseName = link.source.id + '-' + link.dest.id;
            container.select(`#${baseName}`).style({
                'stroke-dashoffset': 0,
                'stroke-width': linkWidthScale(Math.abs(link.weight.get())),
                stroke: colorScale(link.weight.get()),
            });
            const val = container.select(`#val-${baseName}`);
            if (!val.empty()) {
                val.text(link.weight.getWithPrecision(state.precision, state.weightSuppressMultOp));
                drawValuesBox(val, link.weight.get());
            }
        });
    }

    function updateNodesUI() {
        let colorScale = mkColorScaleBias();

        network.forEachNode(true, (node) => {
            D3.select(`#bias-${node.id}`).style('fill', colorScale(node.bias.get()));
            let val = D3.select(svgId).select(`#val-${node.id}${tabSuffix}`);
            if (!val.empty()) {
                val.text(node.bias.getWithPrecision(state.precision, state.weightSuppressMultOp));
                drawValuesBox(val, node.bias.get());
            }
        });
        if (focusNode !== undefined && focusNode !== null) {
            D3.select('#nn-show-math').html(
                focusNode.id + ' = ' + (state.inputs.includes(focusNode.id) ? focusNode.output : focusNode.genMath(state.activationKey))
            );
        } else if (exploreType == ExploreType.NEURON) {
            D3.select('#nn-show-math').html(
                currentDebugNode.id +
                    ' = ' +
                    (state.inputs.includes(currentDebugNode.id) ? currentDebugNode.output : currentDebugNode.genMath(state.activationKey))
            );
        } else {
            D3.select('#nn-show-math').html('');
        }
    }
}

function selectDefaultId(forHidden?: boolean, layerIdx?: number): string {
    let i = 1;
    while (true) {
        let id = forHidden ? 'h' + (layerIdx != null ? layerIdx : i++) + 'n' + i++ : 'n' + i++;
        if (forHidden) {
            if (!state.hiddenNeurons.find((layer) => layer.find((neuron) => neuron === id))) {
                return id;
            }
        } else if (state.inputs.indexOf(id) <= -1 && state.outputs.indexOf(id) <= -1) {
            return id;
        }
    }
}

function mkWidthScale(): _D3.scale.Linear<number, number> {
    let maxWeight = 0;
    function updMaxWeight(link: Link): void {
        let absLinkWeight = Math.abs(link.weight.get());
        if (absLinkWeight > maxWeight) {
            maxWeight = absLinkWeight;
        }
    }
    network.forEachLink(updMaxWeight);
    return D3.scale.linear().domain([0, maxWeight]).range([2, state.weightArcMaxSize]).clamp(true);
}

function mkColorScaleWeight(): _D3.scale.Linear<string, number> {
    let maxWeight = 0;
    function updMaxWeight(link: Link): void {
        let absLinkWeight = Math.abs(link.weight.get());
        if (absLinkWeight > maxWeight) {
            maxWeight = absLinkWeight;
        }
    }
    network.forEachLink(updMaxWeight);
    return D3.scale
        .linear<string, number>()
        .domain([-0.01, 0, +0.01])
        .range(['#f59322', '#222222', '#0877bd'])
        .clamp(true);
}

function mkColorScaleBias(): _D3.scale.Linear<string, number> {
    return D3.scale.linear<string, number>().domain([-1, 0, 1]).range(['#f59322', '#eeeeee', '#0877bd']).clamp(true);
}

function drawValuesBox(text: D3Selection, valueForColor: number, forNodeOutput?: boolean): void {
    const rect = D3.select('#rect-' + text.attr('id'));
    const bbox = (text.node() as any).getBBox();
    rect.attr('x', bbox.x - 4);
    rect.attr('y', bbox.y);
    rect.attr('width', bbox.width + 8);
    rect.attr('height', bbox.height);
    if (forNodeOutput) {
        rect.style('fill', 'white');
        rect.attr({ ry: '10%', stroke: val2color(valueForColor), 'stroke-width': 4, 'stroke-opacity': 1 });
    } else {
        rect.style('fill', val2color(valueForColor));
    }

    function val2color(val: number): string {
        return val < 0 ? '#f5932260' : val == 0 ? '#e8eaeb60' : '#0877bd60';
    }
}

function makeNetworkFromState() {
    network = new Network(state);
}

function nodeOrLink2Value(nodeOrLink: Node | Link): string {
    return nodeOrLink instanceof Link
        ? nodeOrLink.weight.getWithPrecision('*', state.weightSuppressMultOp)
        : nodeOrLink instanceof Node
        ? nodeOrLink.bias.getWithPrecision('*', state.weightSuppressMultOp)
        : '';
}

function value2NodeOrLink(nodeOrLink: Node | Link, value: string) {
    if (value != null) {
        if (nodeOrLink instanceof Link) {
            nodeOrLink.weight.set(value);
        } else if (nodeOrLink instanceof Node) {
            nodeOrLink.bias.set(value);
        } else {
            throw 'invalid nodeOrLink';
        }
        state.weights = network.getWeightArray();
        state.biases = network.getBiasArray();
        updateUI('');
    }
}

/**
 * remember, that a new program was imported into the program tab. In this case -if the simulation tab is open- at simulation close time the NN must
 * not be written back to the blockly XML.
 */
export function programWasReplaced(): void {
    rememberProgramWasReplaced = true;
}

/**
 * extract data from the network and put it into the state and store the state in the start block
 */
export function saveNN2Blockly(neuralNetwork?: Network): void {
    if (rememberProgramWasReplaced) {
        return; // program was imported. Old NN should NOT be saved
    }
    let networkToSave: Network = neuralNetwork ? neuralNetwork : network;
    var startBlock = UTIL.getTheStartBlock();
    try {
        state.weights = networkToSave.getWeightArray();
        state.biases = networkToSave.getBiasArray();
        state.inputs = networkToSave.getInputNames();
        state.hiddenNeurons = networkToSave.getHiddenNeuronNames();
        state.outputs = networkToSave.getOutputNames();
        startBlock.data = JSON.stringify(state);
    } catch (e) {
        LOG.error('failed to create a JSON string from nn state');
    }
}

export function getNetwork(): Network {
    return network;
}
