import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

// noinspection CssUnresolvedCustomProperty,HtmlUnknownAttribute
class SkillTree extends PolymerElement {

    // noinspection JSUnusedGlobalSymbols
    static get template() {
        return html`
            <style>
                :host {
                    --skill-tree-button-background: #1A469D;
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
                    filter: blur(0px);
                }

                .skill-tree-button {
                    font-family: minecraft-regular, sans-serif;
                    font-size: 1.5em;
                    position: relative;
                    padding: 0;
                    margin: 0;
                    outline: none;
                    border: 0;
                    width: 60px;
                    height: 60px;
                    border-radius: 0;
                    /*noinspection CssUnknownTarget*/
                    transition: border linear 0.1s, background-color linear 0.1s, scale linear 0.05s;
                    align-content: center;
                    user-select: none;
                    box-shadow: 0 0 50px #0005;
                }

                .skill-tree-button-background {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background-color: currentColor;
                    filter: saturate(0.4);
                    transition: 0.12s;
                }

                .skill-tree-button-icon {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    /*noinspection CssUnknownTarget*/
                    background-image: var(--button-icon);
                    background-repeat: no-repeat, no-repeat;
                    background-size: 80%, 50%;
                    background-position: center;
                    pointer-events: none;
                    filter: saturate(0.4);
                    transition: 0.12s;
                }

                .skill-tree-button[skilled] .skill-tree-button-background {
                    filter: saturate(1.2);
                }

                .skill-tree-button[skilled] .skill-tree-button-icon {
                    filter: saturate(1.2);
                }

                .skill-tree-button[skillable]:hover .skill-tree-button-background {
                    filter: saturate(1.2);
                }

                .skill-tree-button[skillable]:hover .skill-tree-button-icon {
                    filter: saturate(1.2);
                }

                .skill-tree-button[skilled]:hover, .skill-tree-button[start]:hover {
                    cursor: default;
                }

                .skill-tree-button[skillable] {
                    cursor: pointer;
                }

                .skill-tree-button:not(:hover[skillable]) {
                    transition: background-color linear 0.5s;
                }

                .skill-tree-button[skilled] {
                    box-shadow: 0 0 15px currentColor;
                }

                .skill-tree-button:hover[skillable] {
                    box-shadow: 0 0 30px currentColor;
                }

                .skill-tree-button[start] {
                    /*noinspection CssUnknownTarget*/
                    background-image: url("./assets/start_node.webp");
                    background-size: cover;
                    background-repeat: no-repeat;
                    background-position: center center;
                    box-shadow: 0 0 30px #c77e4f77;
                    border-radius: 0;
                    border: 0;
                    width: 90px;
                    height: 90px;
                    filter: saturate(1.2);
                }

                .skill-tree-button[start] .skill-tree-button-icon {
                    filter: saturate(1.2);
                }

                .skill-tree-button[start] .skill-tree-button-background {
                    filter: saturate(1.2);
                }

                .skill-tree-button:not([start])::after {
                    position: absolute;
                    left: 0;
                    top: 0;
                    width: 100%;
                    height: 100%;
                    content: "";
                    /*noinspection CssUnknownTarget*/
                    background-image: url("./assets/shadow.webp");
                    background-size: 100%;
                }

                .skill-tree-connections {
                    position: absolute;
                    width: 100%;
                    height: 100%;
                    background-color: #0000;
                }

                .skill-tree-connection {
                    stroke: var(--skill-tree-button-background);
                    stroke-width: 0;
                    transition: linear 0.5s;
                }

                .skill-tree-connection[unlocked] {
                    stroke: var(--skill-tree-button-background-skilled);
                    stroke-width: 5;
                }

                .skill-tree-connection[skillable] {
                    stroke: #de2b96;
                    stroke-width: 2;
                }

                .skill-tree-skill-tooltip {
                    visibility: hidden;
                    position: relative;
                    display: inline-block;
                    width: 350px;
                    color: var(--skill-tree-text);
                    /*noinspection CssUnknownTarget*/
                    background-image: url("./assets/signtexture.png");
                    background-size: 500px;
                    font-size: 1.35em;
                    padding: 20px;
                    user-select: none;
                    filter: saturate(1.2);
                }

                .skill-tree-button .skill-tree-skill-tooltip {
                    visibility: hidden;
                    position: absolute;
                    top: calc(30px - 25%);
                    left: 60px;
                }


                .skill-tree-button:hover .skill-tree-skill-tooltip {
                    visibility: visible;
                    z-index: 1;
                }

                .skill-tree-skill-tooltip-header {
                    font-size: 1.5em;
                    text-align: left;
                }

                .divbg {
                    position: absolute;
                    width: 100%;
                    height: 100%;
                    /*noinspection CssUnknownTarget*/
                    background-image: url("./assets/background.png");
                    background-size: 1024px;
                    image-rendering: pixelated;
                    background-repeat: repeat;
                    overflow: hidden;
                }

                .skill-tree-skillpoints {
                    position: absolute;
                    top: 10px;
                    left: 10px;
                    color: white;
                    font-size: 2.5em;
                    font-family: minecraft-regular, sans-serif;
                    z-index: 3;
                    user-select: none;
                }
            </style>
            <!--suppress CssOverwrittenProperties -->
            <div class="divbg" style="background-position: top {{yOffset}}px left {{xOffset}}px;">
                <template is="dom-repeat" items="{{skillConnections}}">
                    <svg class="skill-tree-connections">
                        <line unlocked$="{{item.unlocked}}"
                              skillable$="{{item.skillable}}"
                              x1$="calc({{item.x1}} + {{xOffset}} + 30)"
                              y1$="calc({{item.y1}} + {{yOffset}} + 30)"
                              x2$="calc({{item.x2}} + {{xOffset}} + 30)"
                              y2$="calc({{item.y2}} + {{yOffset}} + 30)"
                              class="skill-tree-connection"
                        />
                    </svg>
                </template>
                <template is="dom-repeat" items="{{skillNodes}}">
                    <button id$="skill-{{item.id}}" class="skill-tree-button" on-click="handleSkillClick"
                            on-mouseup="mouseOutOfSkillNode"
                            style$="
                                position:absolute;
                                overflow: visible;
                                top:calc({{yOffset}}px + {{item.y}}px);
                                left:calc({{xOffset}}px + {{item.x}}px);
                                color: {{item.color}};"
                            skilled$="{{item.unlocked}}"
                            disabled$="{{!item.skillable}}"
                            start$="{{item.start}}"
                            skillable$="{{item.skillable}}"
                            on-mouseover="mouseOverSkillNode"
                            on-mouseout="mouseOutOfSkillNode">

                        <div class="skill-tree-button-background"></div>
                        <div class="skill-tree-button-icon"
                             style$="--button-icon: url('./assets/skillicons/{{item.icon}}');"></div>
                        <span class="skill-tree-skill-tooltip">
                            <span class="skill-tree-skill-tooltip-header">
                                {{item.label}}
                            </span>
                        <br><br>
                        {{item.description}}
                        </span>
                    </button>
                </template>
                <div class="skill-tree-skillpoints">
                    Skillpoints: {{skillpoints}}
                </div>
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
                        font-family: minecraft-regular;
                        /*noinspection CssUnknownTarget*/
                        src: url("./assets/MinecraftRegular.otf") format("opentype");
                    }
                </style>
            </custom-style>`;

        document.head.appendChild($_documentContainer.content);
    }

    tokenCheck() {
        this.$server.tokenCheck();
    }

    ready() {
        super.ready();
        this.xOffset = window.innerWidth / 2 - 30;
        this.yOffset = window.innerHeight / 2 - 30;

        this.tokenTimer = setInterval(this.tokenCheck.bind(this), 2000);
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