package my.testpackage;

@eu.mihosoft.vrl.instrumentation.VRLVisualization
public class MyFileClass {
    
    public int m1(int v1) {
        m1(v1);
    }
    public int m2(int v1, int v2) {
        //        m1(v1);
        //        m1(v2);
        m2(m1(v1), m1(v2));
    }
    public int m3(int v1, int v2, int v3) {
        m1(v1);
        for(int i = 0; i <= 30; i++) {
            m3(v1, v2, m2(v2, v3));
        }
        
        for(int j = 2; j <= 3; j++) {
            m3(v1, v2, m2(v2, v3));
        }
        
        m2(m1(v1), m1(v2));
        m2(m1(v1), m1(v2));
        m1(v1);
        
        for(int k = 3; k <= 5; k++) {
            m3(v1, v2, m2(v2, v3));
        }
    }
}
@eu.mihosoft.vrl.instrumentation.VRLVisualization
public class MyFileClass2 {
    
    public int abc123(int a) {
    }
    public int abc1234(int a) {
    }
    public int m1(int v1) {
        m1(v1);
    }
    public int m2(int v1, int v2) {
        m1(v1);
        m1(v2);
        m2(v1, v2);
    }
    //<!VRL!> my special comment
    public int m3(int v1, int v2, int v3) {
        m2(v1, v2);
        m3(v1, v2, v3);
        m1(v1);
        println("1");
    }
    public int abc(int a) {
    }
}
@eu.mihosoft.vrl.instrumentation.VRLVisualization
public class A {
    
    public void abc() {
    }
    public void a123() {
    }
}
@eu.mihosoft.vrl.instrumentation.VRLVisualization
public class B {
    
    public void abc() {
    }
    public void a123() {
    }
}
// <editor-fold defaultstate="collapsed" desc="VRL-Data">
/*<!VRL!><Type:VRL-Layout>
<map>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:abc1234</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m3:inv:m1</string>
    <layout>
      <x>704.6441964063256</x>
      <y>657.0494725665422</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m1</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m3:inv:m2</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m2:inv:m1</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m1:inv:m1</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:abc123</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m2:inv:m2</string>
    <layout>
      <x>155.44654214514003</x>
      <y>211.94893698115638</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m3:inv:m3</string>
    <layout>
      <x>695.08033690119</x>
      <y>457.78689961571</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.A</string>
    <layout>
      <x>1181.580587269691</x>
      <y>931.1743469518818</y>
      <width>550.0</width>
      <height>503.26843633505877</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.B</string>
    <layout>
      <x>1968.0552757133507</x>
      <y>942.4612202473601</y>
      <width>550.0</width>
      <height>800.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m3:for</string>
    <layout>
      <x>73.39521657809345</x>
      <y>139.14179818843996</y>
      <width>400.0</width>
      <height>259.61317217045416</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:abc</string>
    <layout>
      <x>423.37108952951377</x>
      <y>448.52184732334626</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass</string>
    <layout>
      <x>0.0</x>
      <y>507.31905336452314</y>
      <width>1063.643780451588</width>
      <height>1042.7704190217628</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.B:abc</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m2</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m3</string>
    <layout>
      <x>726.4184408526169</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2</string>
    <layout>
      <x>1065.595793193626</x>
      <y>0.0</y>
      <width>1220.421598292887</width>
      <height>860.236342502569</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass2:m3:inv:println</string>
    <layout>
      <x>1395.211771290391</x>
      <y>788.7401451111365</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m1:inv:m1</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>893.25</width>
      <height>977.25</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m1</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>435.9632529082364</width>
      <height>382.0612281360378</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m2:inv:m2</string>
    <layout>
      <x>747.9520294977849</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m3:inv:m1</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m2</string>
    <layout>
      <x>468.95111560589817</x>
      <y>0.0</y>
      <width>932.9157515412787</width>
      <height>681.1564477435569</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.A:abc</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m2:inv:m1</string>
    <layout>
      <x>0.0</x>
      <y>26.998517613932762</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m3</string>
    <layout>
      <x>713.216916064489</x>
      <y>774.4037231884298</y>
      <width>936.235179687246</width>
      <height>908.979007823044</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.B:a123</string>
    <layout>
      <x>404.5740125995741</x>
      <y>482.1635492625061</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m3:for:inv:m2</string>
    <layout>
      <x>0.0</x>
      <y>0.0</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.MyFileClass:m3:for:inv:m3</string>
    <layout>
      <x>630.0976156057211</x>
      <y>411.8243168106232</y>
      <width>400.0</width>
      <height>100.0</height>
      <contentVisible>true</contentVisible>
    </layout>
  </entry>
  <entry>
    <string>Script:my.testpackage.A:a123</string>
    <layout>
      <x>523.3454003490184</x>
      <y>402.08244173156294</y>
      <width>400.0</width>
      <height>300.0</height>
      <contentVisible>false</contentVisible>
    </layout>
  </entry>
</map>
*/
// </editor-fold>