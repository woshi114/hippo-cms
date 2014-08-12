/*
 *  Copyright 2011-2014 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @description
 * <p>
 * Image cropper
 * </p>
 * @namespace YAHOO.hippo
 * @requires yahoo, dom, hippowidget, imagecropper
 * @module hippoimagecropper
 * @beta
 */


YAHOO.namespace('hippo');

if (!YAHOO.hippo.ImageCropper) {
    (function() {
        var Dom = YAHOO.util.Dom, Lang = YAHOO.lang;

        YAHOO.hippo.ImageCropper = function(id, config) {
            YAHOO.hippo.ImageCropper.superclass.constructor.apply(this, arguments);

            this.regionInputId = config.regionInputId;
            this.imagePreviewContainerId = config.imagePreviewContainerId;

            this.initialX = config.initialX;
            this.initialY = config.initialY;
            this.minimumWidth = config.minimumWidth;
            this.minimumHeight = config.minimumHeight;

            this.originalImageWidth = config.originalWidth;
            this.originalImageHeight = config.originalHeight;
            this.thumbnailWidth = config.thumbnailWidth;
            this.thumbnailHeight = config.thumbnailHeight;

            this.upscalingEnabled = config.upscalingEnabled;
            this.previewVisible = config.previewVisible;
            this.status = config.status;

            this.fixedDimension = config.fixedDimension;
            this.thumbnailSizeLabelId = config.thumbnailSizeLabelId;

            if (!this.upscalingEnabled) {
                this.minimumWidth = this.thumbnailWidth;
                this.minimumHeight = this.thumbnailHeight;
            }

            // Set the max preview width and height (used to determine the scaling of the preview container/image)
            this.maxPreviewWidth = 200;
            this.maxPreviewHeight = 300;

            this.cropper = null;
            this.previewImage = null;
            this.previewContainer = null;
            this.previewLabelTemplate = null;
        };

        YAHOO.extend(YAHOO.hippo.ImageCropper, YAHOO.hippo.Widget, {

            render: function() {
                var scalingFactor;
                if (this.previewVisible) {
                    this.previewImage = Dom.getFirstChild(this.imagePreviewContainerId);
                    this.previewContainer = Dom.get(this.imagePreviewContainerId);

                    //initial values
                    Dom.setStyle(this.previewImage, 'top',  '-' + this.initialX + 'px');
                    Dom.setStyle(this.previewImage, 'left', '-' + this.initialY + 'px');

                    scalingFactor = this.determinePreviewScalingFactor(this.thumbnailWidth, this.thumbnailHeight);
                    Dom.setStyle(this.previewImage, 'width',  Math.floor(scalingFactor * this.originalImageWidth) + 'px');
                    Dom.setStyle(this.previewImage, 'height', Math.floor(scalingFactor * this.originalImageHeight) + 'px');
                }
                this.previewLabelTemplate = Dom.get(this.thumbnailSizeLabelId).innerHTML;

                this.cropper = new YAHOO.widget.ImageCropper(this.id,
                        {
                            keyTick:4,
                            initialXY:[this.initialX, this.initialY],
                            initHeight: this.thumbnailHeight,
                            initWidth: this.thumbnailWidth,
                            ratio: this.fixedDimension == 'both',
                            minWidth: this.minimumWidth,
                            minHeight: this.minimumHeight,
                            status : this.status
                        }
                );
                this.cropper.on('moveEvent', this.onMove, null, this);

                this.updateRegionInputValue(this.cropper.getCropCoords());
                this.updatePreviewLabel(this.thumbnailWidth, this.thumbnailHeight);
            },

            onMove : function(e) {
                var coords = this.cropper.getCropCoords();
                this.updateRegionInputValue(coords);
                this.updatePreviewImage(coords);
            },

            updatePreviewImage : function(coords) {
                var targetScalingFactor = 1, scalingFactor,
                    previewImageWidth, previewImageHeight,
                    previewContainerWidth, previewContainerHeight;

                if (this.fixedDimension == 'both') {
                    // Since the ratio is fixed, both height and width change by the same percentage
                    targetScalingFactor = this.thumbnailWidth / coords.width;
                    previewImageWidth = this.thumbnailWidth;
                    previewImageHeight = this.thumbnailHeight;
                } else if (this.fixedDimension == 'width') {
                    targetScalingFactor = this.thumbnailWidth / coords.width;
                    previewImageWidth = this.thumbnailWidth;
                    previewImageHeight = Math.floor(targetScalingFactor * coords.height);
                } else if (this.fixedDimension == 'height') {
                    targetScalingFactor = this.thumbnailHeight / coords.height;
                    previewImageWidth = Math.floor(targetScalingFactor * coords.width);
                    previewImageHeight = this.thumbnailHeight;
                }

                // Check for scaling to max preview width
                scalingFactor = this.determinePreviewScalingFactor(coords.width, coords.height);
                if (scalingFactor < targetScalingFactor) {
                    previewContainerWidth = Math.floor(scalingFactor * coords.width);
                    previewContainerHeight = Math.floor(scalingFactor * coords.height);
                } else {
                    scalingFactor = targetScalingFactor;
                    previewContainerWidth = previewImageWidth;
                    previewContainerHeight = previewImageHeight;
                }

                this.updatePreviewLabel(previewImageWidth, previewImageHeight);

                if (this.previewVisible && this.previewImage != null) {
                    this.updatePreviewImageDimensions(coords, previewContainerWidth, previewContainerHeight, scalingFactor);
                }
            },

            updatePreviewImageDimensions : function(coords, previewWidth, previewHeight, scalingFactor) {
                // set the preview box dimensions
                Dom.setStyle(this.previewContainer, 'width',  previewWidth + 'px');
                Dom.setStyle(this.previewContainer, 'height', previewHeight + 'px');

                var w = Math.floor(this.originalImageWidth  * scalingFactor),
                        h = Math.floor(this.originalImageHeight * scalingFactor),
                        x = Math.floor(coords.top  * scalingFactor),
                        y = Math.floor(coords.left * scalingFactor);

                Dom.setStyle(this.previewImage, 'top',   '-' + x + 'px');
                Dom.setStyle(this.previewImage, 'left',  '-' + y + 'px');
                Dom.setStyle(this.previewImage, 'width',  w + 'px');
                Dom.setStyle(this.previewImage, 'height', h + 'px');
            },

            determinePreviewScalingFactor : function(previewWidth, previewHeight) {
                var widthBasedScaling = 1, heightBasedScaling = 1;

                if (previewWidth > this.maxPreviewWidth) {
                    widthBasedScaling = this.maxPreviewWidth / previewWidth;
                }

                if (previewHeight > this.maxPreviewHeight) {
                    heightBasedScaling = this.maxPreviewHeight / previewHeight;
                }

                if (heightBasedScaling < widthBasedScaling) {
                    return heightBasedScaling;
                } else {
                    return widthBasedScaling;
                }
            },

            updateRegionInputValue : function(coords) {
                var regionInput = Dom.get(this.regionInputId);
                if (regionInput) {
                    regionInput.value = Lang.JSON.stringify(coords);
                }
            },

            updatePreviewLabel : function(w, h) {
                var label = this.previewLabelTemplate.replace('width', w).replace('height', h);
                Dom.get(this.thumbnailSizeLabelId).innerHTML = label;
            }

        });
    })();

    YAHOO.register("HippoImageCropper", YAHOO.hippo.ImageCropper, {
        version: "2.9.0", build: "2800"
    });
}
