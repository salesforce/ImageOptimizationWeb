# ImageOptimizationWeb - Website for optimizing images #

Copyright (c) 2014, Salesforce.com. All rights reserved.

Created by <span itemscope="" itemtype="http://schema.org/Person">
	<a itemprop="url" rel="author" href="https://github.com/eperret"><span itemprop="name">Eric Perret</span></a>
</span>

## Summary ##

ImageOptimizationWeb is a web based version of the [ImageOptimization](https://git.soma.salesforce.com/perfeng/ImageOptimization) java batch code use to optimize images that are uploaded to it. It does the optimizations using a process is called [lossless compression](http://en.wikipedia.org/wiki/Image_compression#Lossy_and_lossless_compression).

Apart from optimizing an image, it also supports a few other things
* Converting image types, GIFs to PNGs, if it will make the image smaller.
* Create a Chrome specific verison, [WebP](https://developers.google.com/speed/webp/?csw=1)

When optimizaing or converting an image, depending on the size it will take while for the image to be compressed. The process was designed for the smallest file size not the diration of optimization. The app is also highly distributed and will take advantage of multiple CPU cores if more than 1 image is being compressed at a time. Lastly it will involve a fare amount of disk I/O because of the optimizing it is doing.

## Installation ##

NOTE: Currently this code only works on Linux due to [binary dependancies](https://git.soma.salesforce.com/perfeng/ImageOptimization/tree/master/lib/binary/linux) in the ImageOptimization project this code depends on.

See the Image Optimization [installation instructions]() for the steps needed to download and compile the binaries.

The file system path of the image optimization binaries needs to set in the the [sping-servlet.xml](https://git.soma.salesforce.com/perfeng/ImageOptimizationWeb/blob/master/WebContent/WEB-INF/spring-servlet.xml) file as the constructor argument for the "imageOptimizationService" bean.

    <bean id="imageOptimizationService" class="com.salesforce.perfeng.uiperf.imageoptimization.service.ImageOptimizationService" factory-method="createInstance" destroy-method="destroy">
        <constructor-arg value="~/git/ImageOptimization/lib/binary/linux/"/>
    </bean>

You need to have JAVA JDK 7 on the machine for comipling and Maven 3.  Then run `mvn install` and it will packpage up all of the content, with the exception of the binary files, and create a WAR file which is ready to be deployed to your webserver.

## How to use ##

To access the web app run `http://localhost/optimize/images/` from a modern browser to launch the web interface.  Currently it has only been tested on IE 10+ and Chrome.  Please file [bugs](https://github.com/forcedotcom/ImageOptimizationWeb/issues) if you find any issues with other browsers.
