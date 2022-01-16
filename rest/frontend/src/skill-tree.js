import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

// noinspection CssUnresolvedCustomProperty
class SkillTree extends PolymerElement {

    // noinspection JSUnusedGlobalSymbols
    static get template() {
        return html`
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
                    cursor: grab;
                    overflow: hidden;
                    overscroll-behavior: none;
                }

                .skill-tree-button {
                    font-family: minecraft-font, sans-serif;
                    font-size: 1.5em;
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
                    align-content: center;
                    z-index: 1;
                }

                .skill-tree-button:hover:not([skilled, start]) {
                    box-shadow: 0 0 6px #9ecaed60;
                    background-color: var(--skill-tree-button-background-hover);
                    cursor: pointer;
                }

                .skill-tree-button[skilled] {
                    background-color: var(--skill-tree-button-background-skilled);
                    box-shadow: 0 0 15px #DE372B;
                }

                .skill-tree-button[start] {
                    background-color: red;
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
                    width: 350px;
                    color: var(--skill-tree-text);
                    /*noinspection CssUnknownTarget*/
                    background-image: url("./assets/signtexture.png");
                    background-size: 500px;
                    font-size: 1.35em;
                    padding: 20px;
                }

                .skill-tree-button .skill-tree-skill-tooltip {
                    visibility: hidden;
                    position: absolute;
                    top: calc(30px - 25%);
                    left: 60px;
                    z-index: 2;
                }


                .skill-tree-button:hover .skill-tree-skill-tooltip {
                    visibility: visible;
                    position: absolute;
                }

                .skill-tree-skill-tooltip-header {
                    font-size: 1.5em;
                    text-align: left;
                }

                .divbg {
                    width: 100%;
                    height: 100%;
                    /*noinspection CssUnknownTarget*/
                    background-image: url("./assets/background.png");
                    background-size: 150px;
                    image-rendering: pixelated;
                    background-repeat: repeat;
                }
            </style>
            <!--suppress CssOverwrittenProperties -->
            <div class="divbg" style="background-position: top {{yOffset}}px left {{xOffset}}px;">
                <template is="dom-repeat" items="{{skillNodes}}">
                    <button id$="skill-{{item.id}}" class="skill-tree-button" on-click="handleSkillClick"
                            style$="top:calc({{yOffset}}px + {{item.y}}px); left:calc({{xOffset}}px + {{item.x}}px)"
                            skilled$="{{item.unlocked}}"
                            disabled$="{{item.unlocked}}"
                            start$="{{item.start}}"
                            on-mouseover="mouseOverSkillNode"
                            on-mouseout="mouseOutOfSkillNode">{{item.label}}

                        <span class="skill-tree-skill-tooltip">
                        <span class="skill-tree-skill-tooltip-header">
                            {{item.label}}
                        </span>
                        <br><br>
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
            </div>
        `;
    }

    static get is() {
        return 'skill-tree';
    }

    // noinspection JSUnusedGlobalSymbols
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
            }
        };
    }

    constructor() {
        super();

        this.addEventListener('mousedown', this.mousedown.bind(this));
        this.addEventListener('mouseup', this.mouseup.bind(this));
        this.addEventListener('mousemove', this.mousemove.bind(this));


        this.addCustomFonts();
    }

    addCustomFonts() {
        // custom font requires the custom-style tags around css styles
        const $_documentContainer = document.createElement('template');
        $_documentContainer.innerHTML = `
            <custom-style>
               <style>
                    @font-face {
                        font-family: minecraft-font;
                        /*noinspection CssUnknownTarget*/
                        src: url("./assets/MinecraftFont.ttf") format("truetype");
                    }
                </style>
            </custom-style>`;

        document.head.appendChild($_documentContainer.content);
    }

    ready() {
        super.ready();
        this.xOffset = window.innerWidth / 2 - 30;
        this.yOffset = window.innerHeight / 2 - 30;
    }

    // noinspection JSUnusedGlobalSymbols
    mouseOverSkillNode() {
        if (!this.mousePressed) {
            this.isMouseOverSkillNode = true;
        }
    }

    // noinspection JSUnusedGlobalSymbols
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