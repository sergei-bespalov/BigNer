package com.s99.beatbox;

public class Sound {
    private String mAssetPath;
    private String mName;

    private Integer mSoundId;

    public Sound(String assetsPath){
        mAssetPath = assetsPath;
        String[] components = assetsPath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".wav","");
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }


    public Integer getSoundId() {
        return mSoundId;
    }

    public void setSoundId(Integer soundId) {
        mSoundId = soundId;
    }
}
