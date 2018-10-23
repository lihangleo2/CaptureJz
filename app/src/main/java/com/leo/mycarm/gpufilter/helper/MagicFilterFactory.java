package com.leo.mycarm.gpufilter.helper;


import com.leo.mycarm.gpufilter.basefilter.GPUImageFilter;
import com.leo.mycarm.gpufilter.filter.MagicAntiqueFilter;
import com.leo.mycarm.gpufilter.filter.MagicBlackCatFilter;
import com.leo.mycarm.gpufilter.filter.MagicCalmFilter;
import com.leo.mycarm.gpufilter.filter.MagicHealthyFilter;
import com.leo.mycarm.gpufilter.filter.MagicLatteFilter;
import com.leo.mycarm.gpufilter.filter.MagicNostalgiaFilter;
import com.leo.mycarm.gpufilter.filter.MagicRomanceFilter;
import com.leo.mycarm.gpufilter.filter.MagicSakuraFilter;
import com.leo.mycarm.gpufilter.filter.MagicSkinWhitenFilter;
import com.leo.mycarm.gpufilter.filter.MagicSunriseFilter;
import com.leo.mycarm.gpufilter.filter.MagicSunsetFilter;
import com.leo.mycarm.gpufilter.filter.MagicSweetsFilter;
import com.leo.mycarm.gpufilter.filter.MagicTenderFilter;
import com.leo.mycarm.gpufilter.filter.MagicWarmFilter;
import com.leo.mycarm.gpufilter.filter.MagicWhiteCatFilter;


public class MagicFilterFactory {

    private static MagicFilterType filterType = MagicFilterType.NONE;

    public static GPUImageFilter initFilters(MagicFilterType type) {
        if (type == null) {
            return null;
        }
        filterType = type;
        switch (type) {
            case WHITECAT:
                return new MagicWhiteCatFilter();
            case BLACKCAT:
                return new MagicBlackCatFilter();
            case SKINWHITEN:
                return new MagicSkinWhitenFilter();
            case ROMANCE:
                return new MagicRomanceFilter();
            case SAKURA:
                return new MagicSakuraFilter();

            case ANTIQUE:
                return new MagicAntiqueFilter();
            case CALM:
                return new MagicCalmFilter();

            case HEALTHY:
                return new MagicHealthyFilter();
            case LATTE:
                return new MagicLatteFilter();
            case WARM:
                return new MagicWarmFilter();
            case TENDER:
                return new MagicTenderFilter();
            case SWEETS:
                return new MagicSweetsFilter();
            case NOSTALGIA:
                return new MagicNostalgiaFilter();

            case SUNRISE:
                return new MagicSunriseFilter();
            case SUNSET:
                return new MagicSunsetFilter();

            default:
                return null;
        }
    }

    public MagicFilterType getCurrentFilterType() {
        return filterType;
    }


}
