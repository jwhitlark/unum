(ns screenshot
  (:import [java.awt Dimension])
  (:import [java.awt Rectangle])
  (:import [java.awt Robot])
  (:import [java.awt Toolkit])
  (:import [java.awt.image BufferedImage])
  (:import [javax.imageio ImageIO])
  (:import [java.io File]))

(defn captureScreen [fileName]
  (let [sz (.getScreenSize (Toolkit/getDefaultToolkit))
	scRect (Rectangle. sz)
	robot (Robot.)
	image (.createScreenCapture robot scRect)
	fl (File. fileName)]
    (ImageIO/write image "png" fl)))

(captureScreen "test.png")
