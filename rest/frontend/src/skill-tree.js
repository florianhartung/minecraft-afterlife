import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

// noinspection CssUnresolvedCustomProperty
class SkillTree extends PolymerElement {

    static get template() {
        return html`
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Secular+One&display=swap" rel="stylesheet">
            <style>
                :host {
                    --skill-tree-button-background: #1A469D;
                    --skill-tree-button-background-hover: #F05738;
                    --skill-tree-button-background-skilled: #DE372B;
                    --skill-tree-button-border: #F0EFF4;
                    --skill-tree-text: #F0EFF4;
                    --skill-tree-background: #0E2958;
                    --skill-tree-connection: #D22E3E;

                    width: 100%;
                    height: 100vh;
                    background-color: var(--skill-tree-background);
                    cursor: grab;
                    overflow: hidden;
                    overscroll-behavior: none;
                }

                .skill-tree-button {
                    position: absolute;
                    padding: 0;
                    margin: 0;
                    outline: none;
                    border: 1px solid var(--skill-tree-button-border);
                    width: 60px;
                    height: 60px;
                    border-radius: 50%;
                    background-color: var(--skill-tree-button-background);
                    transition: border linear 0.1s, background-color linear 0.1s, scale linear 0.05s;
                    color: var(--skill-tree-text);
                    font-weight: bold;
                    align-content: center;
                    z-index: 1;
                }

                .skill-tree-button:hover:not([skilled]) {
                    box-shadow: 0 0 6px #9ecaed60;
                    background-color: var(--skill-tree-button-background-hover);
                    cursor: pointer;
                }

                .skill-tree-button[skilled] {
                    background-color: var(--skill-tree-button-background-skilled);
                    box-shadow: 0 0 15px #DE372B;
                }

                .skill-tree-connections {
                    position: absolute;
                    width: 100%;
                    height: 100%;
                    background-color: #0000;
                    z-index: 0;
                }

                .skill-tree-connection {
                    stroke: var(--skill-tree-connection);
                    stroke-width: 2;
                }

                .skill-tree-skill-tooltip {
                    position: relative;
                    display: inline-block;
                    border: 2px solid var(--skill-tree-button-border);
                    width: 500px;
                    color: var(--skill-tree-text);
                    background-color: #1A469D;
                    font-size: medium;
                    padding: 20px;
                }

                .skill-tree-button .skill-tree-skill-tooltip {
                    visibility: hidden;
                    position: absolute;
                    top: calc(30px - 25%);
                    left: 80px;
                    z-index: 2;
                }


                .skill-tree-button:hover .skill-tree-skill-tooltip {
                    visibility: visible;
                    position: absolute;
                }

                .skill-tree-skill-tooltip-header {
                    font-size: 2em;
                    text-align: left;
                }

                hr.rounded {
                    border-top: 2px solid var(--skill-tree-text);
                    border-radius: 1px;
                }
            </style>

            <template is="dom-repeat" items="{{skillNodes}}">
                <button class="skill-tree-button" on-click="handleSkillClick"
                        style$="top:calc({{yOffset}}px + {{item.y}}px); left:calc({{xOffset}}px + {{item.x}}px)"
                        skilled$="{{item.unlocked}}"
                        disabled$="{{item.unlocked}}"
                        on-mouseover="mouseOverSkillNode"
                        on-mouseout="mouseOutOfSkillNode">{{item.label}}
                    <span class="skill-tree-skill-tooltip">
                        <span class="skill-tree-skill-tooltip-header">
                            {{item.label}}
                        </span>
                        <hr class="rounded">
                        {{item.description}}
                    </span>
                </button>

            </template>

            <template is="dom-repeat" items="{{skillConnections}}">
                <svg class="skill-tree-connections">
                    <line x1$="calc({{item.x1}} + {{xOffset}} + 30)" y1$="calc({{item.y1}} + {{yOffset}} + 30)"
                          x2$="calc({{item.x2}} + {{xOffset}} + 30)" y2$="calc({{item.y2}} + {{yOffset}} + 30)"
                          class="skill-tree-connection"/>
                </svg>
            </template>
        `;
    }

    static get is() {
        return 'skill-tree';
    }

    static get properties() {
        return {
            xOffset: {
                type: Number,
                value: 0
            },
            yOffset: {
                type: Number,
                value: 0
            },
            mousePressed: {
                type: Boolean,
                value: false
            },
            xOffsetLast: {
                type: Number,
                value: 0
            },
            yOffsetLast: {
                type: Number,
                value: 0
            },
            isMouseOverSkillNode: {
                type: Boolean,
                value: false
            }
        };
    }

    constructor() {
        super();

        this.addEventListener('mousedown', this.mousedown.bind(this));
        this.addEventListener('mouseup', this.mouseup.bind(this));
        this.addEventListener('mousemove', this.mousemove.bind(this));
    }

    mouseOverSkillNode() {
        this.isMouseOverSkillNode = true;
    }

    mouseOutOfSkillNode() {
        this.isMouseOverSkillNode = false;
    }

    mousedown(event) {
        if (!this.isMouseOverSkillNode) {
            this.xOffsetLast = this.xOffset - event.clientX;
            this.yOffsetLast = this.yOffset - event.clientY;
            this.mousePressed = true;
        }
    }

    mouseup() {
        this.mousePressed = false;
    }

    mousemove(event) {
        if (this.mousePressed) {
            event.preventDefault();
            this.xOffset = this.xOffsetLast + event.clientX;
            this.yOffset = this.yOffsetLast + event.clientY;
        }
    }
}

customElements.define(SkillTree.is, SkillTree);