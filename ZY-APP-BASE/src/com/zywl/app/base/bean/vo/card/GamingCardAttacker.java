package com.zywl.app.base.bean.vo.card;

public class GamingCardAttacker extends GamingCard {



    public GamingCardAttacker(){

    }

    public GamingCardAttacker(String name, int hp, int atk, int def, double chc, double chcImpact, double defChc, double speed, double htt, double dodge, double afb, double combo, double leech,long cardId) {
        super(name, hp, atk, def, chc, chcImpact, defChc, speed, htt, dodge, afb, combo, leech,cardId);
        setCardType(1);
    }
    public GamingCardAttacker(String name, int hp, int atk, int def, double chc, double chcImpact, double defChc, double speed, double htt, double dodge, double afb, double combo, double leech,long cardId,int index) {
        super(name, hp, atk, def, chc, chcImpact, defChc, speed, htt, dodge, afb, combo, leech,cardId,index);
        setCardType(1);
    }
}
