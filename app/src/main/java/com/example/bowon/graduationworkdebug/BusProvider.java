package com.example.bowon.graduationworkdebug;

import com.squareup.otto.Bus;

/**
 * Created by bowon on 2017-04-18.
 */

/*Bus 를 사용하기 위한 provider 객체이다
  singleton으로 실행되기 위해 static으로 선언

  */

public class BusProvider {

    public static final Bus BUS = new Bus();

    public static Bus getInstance(){
        return BUS;
    }
    private BusProvider(){
        //no instance
    }


}
